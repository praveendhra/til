# GCP Pub/Sub

## Core Concepts
- **Topic**: Named channel for publishing messages
- **Subscription**: Named attachment to a topic for receiving messages
- **Message**: Data + attributes published to a topic

```
Publisher → [Topic] → Subscription A → Subscriber A
                   → Subscription B → Subscriber B
```

## Delivery Types

### Pull
Subscriber polls for messages. Good for batch processing.
```python
from google.cloud import pubsub_v1
subscriber = pubsub_v1.SubscriberClient()
response = subscriber.pull(subscription=sub_path, max_messages=10)
for msg in response.received_messages:
    process(msg.message.data)
    subscriber.acknowledge(subscription=sub_path, ack_ids=[msg.ack_id])
```

### Push
Pub/Sub sends messages to an HTTPS endpoint. Good for serverless.
```
Pub/Sub → HTTPS POST → Cloud Run / Cloud Functions
```

## Exactly-Once Delivery
Enable on subscription. Pub/Sub deduplicates by message ID.
Combined with idempotent processing = exactly-once semantics.

## Dead Letter Topics
After N delivery attempts, message goes to dead-letter topic.
```bash
gcloud pubsub subscriptions update my-sub \
  --dead-letter-topic=my-dead-letter \
  --max-delivery-attempts=5
```

## Ordering
Enable message ordering per key:
```
Messages with key "user-123" always delivered in order
Messages with key "user-456" independently ordered
```

## Pub/Sub vs Kafka
| Feature | Pub/Sub | Kafka |
|---------|---------|-------|
| Managed | Fully | Self-managed (or Confluent) |
| Ordering | Per-key | Per-partition |
| Retention | 31 days max | Unlimited |
| Scale | Automatic | Manual (partitions) |
| Cost | Per-message | Per-broker |
