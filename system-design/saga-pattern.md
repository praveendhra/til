# Saga Pattern — Managing Distributed Transactions

## The Problem

In a monolith, you use a database transaction:
```sql
BEGIN TRANSACTION;
  INSERT INTO orders (...);
  UPDATE inventory SET stock = stock - 1 WHERE ...;
  INSERT INTO payments (...);
COMMIT;
-- All succeed or all fail — ACID
```

In microservices, these are **separate databases**. You can't use a single transaction.

```
Order Service → Orders DB
Payment Service → Payments DB      ← These are separate databases!
Inventory Service → Inventory DB
```

## The Solution: Saga Pattern

Break the transaction into a sequence of **local transactions**, each with a **compensating action** (undo).

```
Step 1: Create Order (pending)        ← Compensate: Cancel Order
Step 2: Reserve Inventory             ← Compensate: Release Inventory
Step 3: Process Payment               ← Compensate: Refund Payment
Step 4: Confirm Order (confirmed)     ← No compensation needed (final)

If Step 3 fails:
  → Compensate Step 2: Release Inventory
  → Compensate Step 1: Cancel Order
```

## Two Coordination Approaches

### Choreography (Event-Based)

Each service listens for events and decides what to do next.

```
OrderService publishes "OrderCreated"
  → InventoryService hears it, reserves stock, publishes "StockReserved"
    → PaymentService hears it, charges card, publishes "PaymentProcessed"
      → OrderService hears it, marks order as confirmed

If PaymentService fails:
  publishes "PaymentFailed"
    → InventoryService hears it, releases stock
    → OrderService hears it, cancels order
```

**Pros**: Loose coupling, no central coordinator, simple for 2-3 services
**Cons**: Hard to follow the flow, difficult to debug, can become spaghetti with many services

### Orchestration (Central Coordinator)

A **Saga Orchestrator** directs each step explicitly.

```python
class OrderSaga:
    def execute(self, order):
        try:
            # Step 1
            self.order_service.create_order(order)

            # Step 2
            result = self.inventory_service.reserve(order.items)
            if not result.success:
                self.order_service.cancel_order(order.id)
                return SagaResult.FAILED

            # Step 3
            payment = self.payment_service.charge(order.total)
            if not payment.success:
                self.inventory_service.release(order.items)
                self.order_service.cancel_order(order.id)
                return SagaResult.FAILED

            # Step 4
            self.order_service.confirm_order(order.id)
            return SagaResult.SUCCESS

        except Exception:
            # Compensate all completed steps
            self.compensate(order)
            return SagaResult.FAILED
```

**Pros**: Clear flow, easy to test and debug, handles complex workflows
**Cons**: Orchestrator can become complex, single point of coupling

### When to Use Which?

| Criteria | Choreography | Orchestration |
|----------|-------------|---------------|
| Number of services | 2-3 | 4+ |
| Complexity | Simple, linear | Complex, branching |
| Team structure | Each team owns their service | Central platform team |
| Debugging | Harder (distributed) | Easier (centralized logs) |
| Coupling | Loose | Orchestrator knows all services |

## Compensating Actions — The Hard Part

Compensating transactions must be **idempotent** (safe to retry) and **commutative** (order doesn't matter).

| Forward Action | Compensating Action | Notes |
|---------------|--------------------|----|
| Create order | Cancel order | Set status to "cancelled" (don't delete) |
| Reserve inventory | Release inventory | Add stock back |
| Charge payment | Refund payment | Initiate refund |
| Send email | Send correction email | Can't unsend! Best effort |
| Ship package | Initiate return | Some actions are hard to compensate |

### Non-Compensatable Actions

Some actions can't be undone (sending email, shipping). Solutions:
- **Pivot transaction**: Place non-compensatable actions last
- **Retry with timeout**: Keep trying before giving up
- **Manual intervention**: Flag for human review

## Implementation with AWS Step Functions

```json
{
  "StartAt": "CreateOrder",
  "States": {
    "CreateOrder": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:createOrder",
      "Next": "ReserveInventory",
      "Catch": [{"ErrorEquals": ["States.ALL"], "Next": "CancelOrder"}]
    },
    "ReserveInventory": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:reserveInventory",
      "Next": "ProcessPayment",
      "Catch": [{"ErrorEquals": ["States.ALL"], "Next": "CancelOrder"}]
    },
    "ProcessPayment": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:...:processPayment",
      "Next": "ConfirmOrder",
      "Catch": [{"ErrorEquals": ["States.ALL"], "Next": "RefundAndRelease"}]
    },
    "ConfirmOrder": {"Type": "Task", "Resource": "...", "End": true},
    "RefundAndRelease": {
      "Type": "Parallel",
      "Branches": [
        {"StartAt": "ReleaseInventory", "States": {"ReleaseInventory": {"Type": "Task", "Resource": "...", "End": true}}},
        {"StartAt": "RefundPayment", "States": {"RefundPayment": {"Type": "Task", "Resource": "...", "End": true}}}
      ],
      "Next": "CancelOrder"
    },
    "CancelOrder": {"Type": "Task", "Resource": "...", "End": true}
  }
}
```

## Saga vs Two-Phase Commit (2PC)

| Feature | Saga | 2PC |
|---------|------|-----|
| Isolation | No (intermediate states visible) | Yes (all-or-nothing) |
| Performance | Good (async, no locks) | Poor (blocking, holds locks) |
| Availability | High | Low (coordinator SPOF) |
| Complexity | Medium | Low (but fragile) |
| Real usage | Microservices, cloud | Traditional databases |

## Interview Answer

> "The Saga pattern replaces distributed transactions in microservices. Each step has a local transaction and a compensating action. If step 3 fails, we run compensating actions for steps 2 and 1 in reverse. I prefer orchestration over choreography for complex flows because it's easier to debug and test. The key challenges are: designing idempotent compensating actions, handling non-compensatable actions (place them last), and dealing with the lack of isolation (intermediate states are visible to other transactions, so design for eventual consistency)."
