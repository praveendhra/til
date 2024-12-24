# Saga Pattern for Distributed Transactions

## Problem
Traditional ACID transactions don't work across microservices boundaries.

## Solution
A saga is a sequence of local transactions where each step publishes an event to trigger the next step, with compensating transactions for rollback.

## Types

### Choreography (Event-based)
```
Order Service → [OrderCreated] → Payment Service → [PaymentCompleted] → Inventory Service
                                  [PaymentFailed] → Order Service (compensate)
```
- Each service listens to events and acts
- Decentralized, no coordinator
- Can become complex with many services

### Orchestration (Command-based)
```
Saga Orchestrator → Command → Order Service
                  → Command → Payment Service
                  → Command → Inventory Service
```
- Central orchestrator manages the flow
- Easier to understand and debug
- Single point of coordination

## Compensating Transactions
| Step | Action | Compensation |
|------|--------|-------------|
| 1 | Create Order | Cancel Order |
| 2 | Reserve Payment | Refund Payment |
| 3 | Update Inventory | Restore Inventory |
| 4 | Send Notification | Send Cancellation |

## Best Practices
- Make each step idempotent
- Use correlation IDs for tracing
- Implement timeout handling
- Log all saga state transitions
