# Message Queues vs Event Streams — When to Use Which

## Fundamental Difference

```
Message Queue (SQS, RabbitMQ):
  Producer → [Queue] → ONE Consumer processes it → Message deleted
  "Job to be done" — once processed, it's gone

Event Stream (Kafka, Kinesis):
  Producer → [Log] → MANY Consumers read independently → Events retained
  "Fact that happened" — stays in the log, anyone can read it
```

## Detailed Comparison

| Feature | Message Queue | Event Stream |
|---------|-------------|-------------|
| **Delivery** | Each message → ONE consumer | Each event → MANY consumers |
| **Retention** | Deleted after ack | Retained for configured period (days/weeks) |
| **Ordering** | Best-effort (FIFO optional) | Strict order per partition |
| **Replay** | ❌ Cannot replay consumed messages | ✅ Replay from any offset |
| **Consumer model** | Competing consumers | Consumer groups (parallel read) |
| **Throughput** | 1K-100K msg/sec | 100K-1M+ msg/sec |
| **Latency** | Sub-millisecond | Low milliseconds |
| **Backpressure** | Queue depth grows | Consumer falls behind (lag) |

## When to Use Message Queues

### 1. Task/Job Distribution
```
Web Server → [SQS Queue] → Worker 1 (process image)
                          → Worker 2 (process image)
                          → Worker 3 (process image)
Each image processed by exactly ONE worker
```

### 2. Request-Reply Pattern
```
API → [Request Queue] → Processor → [Reply Queue] → API
Correlation ID links request to response
```

### 3. Load Leveling
```
Spike: 10,000 requests/sec
       ↓
[Queue buffers excess]
       ↓
Workers process at steady 1,000/sec
```

**Use SQS or RabbitMQ when:**
- Each message should be processed **exactly once** by **one consumer**
- You need **dead letter queues** for failed messages
- Message order doesn't matter (or use FIFO queues)
- You want **simple, managed** infrastructure

## When to Use Event Streams

### 1. Event-Driven Architecture
```
OrderService → [Kafka: "orders" topic] → PaymentService
                                       → InventoryService
                                       → AnalyticsService
                                       → NotificationService
Each service independently reads the same events
```

### 2. Data Pipeline / ETL
```
App DB changes → [Kafka via CDC] → Data Warehouse
                                 → Elasticsearch
                                 → Machine Learning pipeline
```

### 3. Event Sourcing
```
All state changes stored as events:
  AccountCreated → MoneyDeposited → MoneyWithdrawn → ...
Can replay to rebuild any state at any point in time
```

**Use Kafka or Kinesis when:**
- **Multiple consumers** need the same events
- You need **replay capability** (reprocess from yesterday)
- **Ordering matters** (events within a partition are ordered)
- **High throughput** required (millions of events/sec)
- Building **event-driven microservices**

## AWS Services Comparison

| Service | Type | Throughput | Ordering | Retention |
|---------|------|-----------|----------|-----------|
| **SQS** | Queue | ~3,000 msg/sec (standard) | Best-effort | 14 days max |
| **SQS FIFO** | Queue | 300 msg/sec (3,000 batched) | Strict per group | 14 days max |
| **SNS** | Pub/Sub | Very high | No | No retention (push) |
| **Kinesis** | Stream | 1 MB/sec per shard | Per shard | 24h-365 days |
| **MSK (Kafka)** | Stream | Very high | Per partition | Configurable |
| **EventBridge** | Event bus | Very high | No | 24h replay |

### Common Patterns

```
SNS + SQS (Fan-out + Competing consumers):
  Event → SNS Topic → SQS Queue 1 → Consumer Group A
                    → SQS Queue 2 → Consumer Group B
                    → Lambda Function

EventBridge (Event routing):
  Event → EventBridge → Rule 1 → Lambda
                      → Rule 2 → SQS
                      → Rule 3 → Step Functions
```

## Kafka Architecture (Brief)

```
Kafka Cluster
├── Topic: "orders"
│   ├── Partition 0: [msg1][msg2][msg3][msg4]...
│   ├── Partition 1: [msg1][msg2][msg3]...
│   └── Partition 2: [msg1][msg2]...
│
├── Consumer Group A (3 consumers, 1 per partition)
│   ├── Consumer 1 reads Partition 0
│   ├── Consumer 2 reads Partition 1
│   └── Consumer 3 reads Partition 2
│
└── Consumer Group B (independent, reads everything)
    └── Consumer 1 reads all partitions
```

**Key Kafka concepts:**
- **Partition**: Unit of parallelism and ordering
- **Offset**: Position in a partition (consumer tracks this)
- **Consumer Group**: Consumers share partitions (each partition → 1 consumer)
- **Replication Factor**: Copies across brokers (RF=3 typical)

## Decision Flowchart

```
Multiple consumers for same event?
├─ Yes → Event Stream (Kafka/Kinesis)
└─ No
    ├─ Need to replay past events?
    │  ├─ Yes → Event Stream
    │  └─ No
    │      ├─ Simple job queue / task distribution?
    │      │  └─ Yes → Message Queue (SQS/RabbitMQ)
    │      └─ Event routing with rules?
    │         └─ Yes → EventBridge
    └─ Both fan-out AND queue?
       └─ SNS + SQS pattern
```

## Interview Answer

> "Message queues (SQS, RabbitMQ) are for task distribution where each message is processed by one consumer and then deleted. Event streams (Kafka, Kinesis) are for event-driven architectures where multiple consumers independently read the same events, ordering matters, and you need replay capability. For example, I'd use SQS for background job processing (image resize, email sending) and Kafka for building a data pipeline where order events flow to payment, inventory, analytics, and notification services simultaneously. On AWS, the SNS+SQS fan-out pattern gives you the best of both: fan-out via SNS topics and reliable per-consumer processing via SQS queues."
