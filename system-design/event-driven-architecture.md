# Event-Driven Architecture — Complete Guide

## Core Concepts

Instead of services calling each other directly (request-response), services communicate by producing and consuming **events**.

```
Request-Response (synchronous):
  OrderService ──HTTP──► PaymentService ──HTTP──► InventoryService
  (coupled, slow, fragile)

Event-Driven (asynchronous):
  OrderService → publishes "OrderCreated" event
  PaymentService ← subscribes, processes payment
  InventoryService ← subscribes, reserves stock
  NotificationService ← subscribes, sends email
  (decoupled, fast, resilient)
```

## Event Types

### 1. Domain Events (Fact)
Something that **happened**. Immutable, past tense.
```json
{
  "type": "OrderPlaced",
  "data": {
    "orderId": "ORD-123",
    "customerId": "CUST-456",
    "items": [{"sku": "WIDGET-1", "qty": 2}],
    "total": 49.99
  },
  "metadata": {
    "timestamp": "2025-01-15T10:30:00Z",
    "correlationId": "abc-123",
    "source": "order-service"
  }
}
```

### 2. Commands
A **request** for something to happen. Can be rejected.
```json
{"type": "ProcessPayment", "data": {"orderId": "ORD-123", "amount": 49.99}}
```

### 3. Notifications (Thin Events)
Signal that something happened, but consumer must fetch details.
```json
{"type": "OrderPlaced", "data": {"orderId": "ORD-123"}}
// Consumer calls OrderService API to get full details
```

## Patterns

### Publish-Subscribe (Pub/Sub)
Events go to a **topic**. All subscribers receive a copy.

```
Producer → Topic "orders" → Consumer A (payments)
                          → Consumer B (inventory)
                          → Consumer C (analytics)
```

**Used by**: SNS, Google Pub/Sub, Kafka topics, Redis Pub/Sub

### Event Streaming
Events stored in an **ordered, immutable log**. Consumers can replay from any offset.

```
Partition 0: [e1] [e2] [e3] [e4] [e5] ...
                              ↑
                        Consumer offset
```

**Used by**: Kafka, AWS Kinesis, Azure Event Hubs, Redpanda

### Event Sourcing
Store the entire **history of state changes** as events, not just current state.

```
Traditional:  Account { balance: 150 }
Event-Sourced:
  1. AccountCreated { balance: 0 }
  2. MoneyDeposited { amount: 200 }
  3. MoneyWithdrawn { amount: 50 }
  Current state = replay events = balance: 150
```

See dedicated TIL on Event Sourcing.

## Message Queues vs Event Streams

| Feature | Queue (SQS, RabbitMQ) | Stream (Kafka, Kinesis) |
|---------|----------------------|------------------------|
| Delivery | Each message consumed by ONE consumer | Each event readable by MANY consumers |
| Order | Best-effort (FIFO optional) | Strict order per partition |
| Retention | Deleted after processing | Retained for configurable period |
| Replay | ❌ Cannot replay | ✅ Can replay from any offset |
| Best for | Task distribution, work queues | Event log, data pipeline, audit |

## Delivery Guarantees

| Guarantee | Meaning | How |
|-----------|---------|-----|
| **At-most-once** | May lose messages, never duplicate | Fire and forget, no ack |
| **At-least-once** | Never lose, may duplicate | Ack after processing, retry on failure |
| **Exactly-once** | Never lose, never duplicate | Idempotent consumers + transactions |

### Making Consumers Idempotent

Since at-least-once is the practical default, your consumers MUST handle duplicates:

```python
def process_payment(event):
    # Use event ID as idempotency key
    if db.exists("processed_events", event["id"]):
        return  # Already processed, skip

    charge_customer(event["data"]["amount"])
    db.insert("processed_events", event["id"])  # Mark as processed
```

## Outbox Pattern — Reliable Event Publishing

**Problem**: How to atomically update a database AND publish an event?

```python
# ❌ WRONG: Not atomic — event might publish but DB fails (or vice versa)
def place_order(order):
    db.insert(order)           # Step 1: DB write
    kafka.publish(order_event) # Step 2: Event publish — what if this fails?
```

**Solution**: Write event to an **outbox table** in the same DB transaction, then a separate process publishes events from the outbox.

```python
# ✅ CORRECT: Outbox pattern
def place_order(order):
    with db.transaction():
        db.insert("orders", order)
        db.insert("outbox", {
            "event_type": "OrderPlaced",
            "payload": json.dumps(order),
            "published": False
        })

# Separate process (CDC or poller):
def publish_outbox_events():
    events = db.query("SELECT * FROM outbox WHERE published = FALSE")
    for event in events:
        kafka.publish(event["event_type"], event["payload"])
        db.update("outbox", event["id"], published=True)
```

**Better**: Use CDC (Change Data Capture) with **Debezium** to stream outbox table changes directly to Kafka.

## Dead Letter Queues (DLQ)

When a message fails processing repeatedly, send it to a DLQ for investigation.

```
Main Queue → Consumer → fails 3 times → Dead Letter Queue
                                              ↓
                                        Alert + manual investigation
```

```yaml
# AWS SQS DLQ configuration
{
  "maxReceiveCount": 3,
  "deadLetterTargetArn": "arn:aws:sqs:us-east-1:123456789:orders-dlq"
}
```

## Choreography vs Orchestration

### Choreography (Decentralized)
Each service knows what to do when it sees an event. No central coordinator.

```
OrderCreated → PaymentService processes → PaymentCompleted → InventoryService reserves
```

**Pros**: Loose coupling, services are independent
**Cons**: Hard to track overall flow, debugging is difficult

### Orchestration (Centralized)
A central **orchestrator** (saga coordinator) directs the workflow.

```
OrderSaga:
  1. Command: ProcessPayment → PaymentService
  2. Command: ReserveInventory → InventoryService
  3. Command: SendConfirmation → NotificationService
```

**Pros**: Clear flow, easy to understand and debug
**Cons**: Orchestrator is a single point of coupling

**Rule of thumb**: Use choreography for 2-3 services, orchestration for complex flows (4+ services).

## Interview Answer

> "Event-driven architecture decouples services by having them communicate through events instead of direct API calls. I'd use Kafka as the event backbone for its ordering guarantees and replay capability. The key challenges are: ensuring reliable event publishing (solved by the outbox pattern with Debezium CDC), handling duplicate events (idempotent consumers with deduplication keys), and managing failed events (dead letter queues with alerting). For complex multi-service workflows, I'd use the Saga pattern with orchestration for clear flow control."
