# GCP Pub/Sub

## Architecture
```
Publisher → Topic → Subscription → Subscriber
                  → Subscription → Subscriber
```

## Key Concepts
- **Topic**: Named channel for messages
- **Subscription**: Named resource for receiving messages
- **Ack deadline**: Time to process before redelivery (default 10s)
- **Dead letter topic**: For messages that fail processing

## Python Publisher
```python
from google.cloud import pubsub_v1

publisher = pubsub_v1.PublisherClient()
topic = publisher.topic_path("my-project", "orders")

data = json.dumps({"order_id": "123", "amount": 99.99})
future = publisher.publish(topic, data.encode("utf-8"), source="api")
print(f"Published: {future.result()}")
```

## Python Subscriber
```python
from google.cloud import pubsub_v1

subscriber = pubsub_v1.SubscriberClient()
subscription = subscriber.subscription_path("my-project", "orders-sub")

def callback(message):
    data = json.loads(message.data.decode("utf-8"))
    process_order(data)
    message.ack()

subscriber.subscribe(subscription, callback=callback)
```

## Delivery Guarantees
- **At-least-once**: Default, may have duplicates
- **Exactly-once**: Enable on subscription (higher latency)

## Ordering
- Ordering key ensures messages with same key delivered in order
- Without ordering key, messages delivered in any order
