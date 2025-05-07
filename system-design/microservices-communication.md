# Microservices Communication Patterns

## Synchronous
### REST (HTTP/JSON)
- Simple, universal
- Tight coupling between services
- Latency accumulates in call chains

### gRPC (HTTP/2 + Protocol Buffers)
- 10x faster than REST for inter-service
- Strong typing with proto files
- Streaming support
- Poor browser support (use gRPC-Web)

## Asynchronous
### Message Queue (Point-to-Point)
```
Service A → Queue → Service B
```
- RabbitMQ, SQS, Azure Service Bus
- Exactly-once delivery guarantees
- Decoupled sender and receiver

### Event Bus (Pub/Sub)
```
Service A → Topic → Service B
                  → Service C
                  → Service D
```
- Kafka, SNS, Azure Event Grid
- Multiple consumers per event
- Event replay capability (Kafka)

## Patterns

### Request-Reply (Async)
```
Service A → Request Queue → Service B
Service A ← Reply Queue   ← Service B
```

### Event-Driven Choreography
Services react to events without central control.

### Saga Orchestration
Central coordinator manages the workflow.

## Decision Matrix
| Need | Use |
|------|-----|
| Simple CRUD | REST |
| High throughput inter-service | gRPC |
| Decoupled, reliable delivery | Message Queue |
| Fan-out to many consumers | Pub/Sub |
| Event replay needed | Kafka |
