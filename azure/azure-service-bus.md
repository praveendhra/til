# Azure Service Bus

## Queues vs Topics

### Queues (Point-to-Point)
```
Producer → Queue → Consumer
```
- One consumer per message
- FIFO ordering (with sessions)
- Dead-letter queue for failed messages

### Topics (Pub/Sub)
```
Producer → Topic → Subscription A → Consumer A
                 → Subscription B → Consumer B
```
- Multiple consumers per message
- Filter rules on subscriptions

## Key Features
- **Message sessions** – FIFO guarantee, grouped processing
- **Dead-letter queue** – Failed messages for inspection
- **Scheduled delivery** – Future message delivery
- **Duplicate detection** – Idempotency window
- **Transactions** – Atomic operations across entities

## Python Example
```python
from azure.servicebus import ServiceBusClient, ServiceBusMessage

client = ServiceBusClient.from_connection_string(conn_str)

# Send
with client.get_queue_sender("orders") as sender:
    message = ServiceBusMessage("order-123", session_id="user-456")
    sender.send_messages(message)

# Receive
with client.get_queue_receiver("orders", max_wait_time=5) as receiver:
    for msg in receiver:
        process(msg)
        receiver.complete_message(msg)
```

## vs Event Hubs vs Event Grid
| Feature | Service Bus | Event Hubs | Event Grid |
|---------|------------|-----------|------------|
| Pattern | Message queue | Event streaming | Event routing |
| Ordering | Sessions | Partitions | No |
| Throughput | Medium | Very high | High |
| Retention | 14 days | 7-90 days | 24 hours |
