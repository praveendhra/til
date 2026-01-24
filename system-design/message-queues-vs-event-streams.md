# Message Queues vs Event Streams

## Message Queues (Point-to-Point)
Each message consumed by ONE consumer. Once processed, it's gone.

**Examples**: AWS SQS, Azure Service Bus Queue, RabbitMQ

**Use when**: Task processing, job queues, work distribution
```
Producer → [Queue] → Consumer (one picks it up)
```

## Event Streams (Pub/Sub)
Each event can be consumed by MANY consumers. Events are retained.

**Examples**: Apache Kafka, AWS Kinesis, Azure Event Hubs, GCP Pub/Sub

**Use when**: Event sourcing, real-time analytics, CDC, audit logs
```
Producer → [Topic] → Consumer Group A
                   → Consumer Group B
                   → Consumer Group C
```

## Key Differences

| Feature | Queue | Stream |
|---------|-------|--------|
| Delivery | Once (per message) | Many consumers |
| Retention | Deleted after consumption | Retained for period |
| Ordering | FIFO (with effort) | Per-partition ordering |
| Replay | No | Yes |
| Use case | Work distribution | Event broadcasting |

## Decision Guide
- Need guaranteed single processing? → **Queue (SQS)**
- Need multiple consumers + replay? → **Stream (Kafka/Kinesis)**
- Need both? → Use both! SQS + SNS fan-out pattern on AWS
