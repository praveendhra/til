# Azure Service Bus vs Event Hubs vs Event Grid

## Azure Service Bus
**Enterprise messaging** with queues and topics.

- FIFO ordering, sessions, dead-letter queues
- Exactly-once processing with peek-lock
- Message size: up to 256KB (standard), 100MB (premium)
- **Use for**: Order processing, financial transactions, workflow orchestration
- **Equivalent**: AWS SQS + SNS, RabbitMQ

## Azure Event Hubs
**Big data streaming** platform.

- Millions of events/second
- Retention: 1 to 90 days
- Consumer groups for parallel processing
- Kafka-compatible (drop-in replacement)
- **Use for**: Telemetry ingestion, log aggregation, real-time analytics
- **Equivalent**: AWS Kinesis, Apache Kafka

## Azure Event Grid
**Reactive event routing** service.

- React to Azure resource events (Blob created, VM stopped)
- Sub-second event delivery
- Filter events with advanced patterns
- **Use for**: Serverless event handling, resource automation
- **Equivalent**: AWS EventBridge

## Quick Decision
```
Need reliable queue with transactions? → Service Bus
Need high-throughput streaming? → Event Hubs
Need to react to Azure events? → Event Grid
Need Kafka compatibility? → Event Hubs
```
