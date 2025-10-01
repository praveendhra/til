# Microservices Communication Patterns

## Synchronous

### REST (HTTP/JSON)
- Simple, well-understood, stateless
- **Downside**: Tight coupling, cascading failures
- Use for: CRUD operations, simple request-response

### gRPC (HTTP/2 + Protobuf)
- Binary protocol, much faster than REST
- Streaming support, strong typing
- Use for: Internal service-to-service, low-latency needs
- **Cloud Run, GKE, ECS** all support gRPC

## Asynchronous

### Message Queue (SQS, Service Bus, Pub/Sub)
- Decouple producer and consumer
- Handle spikes via buffering
- Use for: Background jobs, order processing

### Event-Driven (Kafka, EventBridge, Event Grid)
- Publish events, multiple subscribers react
- Use for: Real-time data pipelines, CQRS

## Patterns

### API Gateway
Single entry point → routes to services
- AWS API Gateway / ALB
- Azure API Management
- GCP API Gateway / Apigee

### Service Mesh
Sidecar proxy handles networking (mTLS, retries, observability)
- **Istio**, **Linkerd**, **AWS App Mesh**

### Saga Pattern
Distributed transactions across services using choreography or orchestration.
Each service does its step + publishes event for next service.
If any step fails → compensating transactions to rollback.

### Circuit Breaker
Prevent cascading failures. After N failures, "open" the circuit — fail fast instead of waiting for timeout.
Libraries: **Resilience4j** (Java), **Polly** (.NET), **Hystrix** (deprecated)
