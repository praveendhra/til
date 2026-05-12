# Microservices Communication Patterns

## Synchronous Communication

### REST (HTTP/JSON)
```
Service A ──HTTP GET /users/123──► Service B
           ◄──200 {"name": "John"}──
```

**Pros**: Simple, well-understood, great tooling, cacheable
**Cons**: Coupling (caller must know API), latency chains, availability dependency
**Best for**: CRUD operations, public APIs, simple request-response

### gRPC (HTTP/2 + Protocol Buffers)

```protobuf
service UserService {
  rpc GetUser (GetUserRequest) returns (User);
  rpc ListUsers (ListUsersRequest) returns (stream User);
}

message User {
  string id = 1;
  string name = 2;
  string email = 3;
}
```

**Pros**:
- **10x faster** than REST/JSON (binary serialization, HTTP/2 multiplexing)
- Strong typing with proto files (contract-first)
- Bi-directional streaming
- Code generation for any language

**Cons**: Not browser-friendly (need gRPC-Web proxy), harder to debug (binary), less human-readable

**Best for**: Internal service-to-service, high-throughput, streaming

### REST vs gRPC Comparison

| Feature | REST | gRPC |
|---------|------|------|
| Protocol | HTTP/1.1 or 2 | HTTP/2 |
| Serialization | JSON (text) | Protobuf (binary) |
| Contract | OpenAPI/Swagger | .proto files |
| Streaming | SSE, WebSocket | Built-in (4 types) |
| Browser support | ✅ Native | ⚠️ gRPC-Web |
| Payload size | Larger (text) | ~10x smaller |
| Code generation | Optional | Required |
| Caching | ✅ HTTP caching | ❌ Custom |

## Asynchronous Communication

### Message Queue (Point-to-Point)
```
Service A → [Queue] → Service B
Each message consumed by exactly ONE consumer
```

**Systems**: SQS, RabbitMQ, Azure Service Bus
**Use case**: Task distribution, work queues, background jobs

### Pub/Sub (Fan-out)
```
Service A → [Topic] → Service B
                    → Service C
                    → Service D
Each message delivered to ALL subscribers
```

**Systems**: SNS, Google Pub/Sub, Kafka, Redis Pub/Sub
**Use case**: Event notification, data replication, analytics

## Service Discovery

Services need to find each other. Two approaches:

### Client-Side Discovery
```
Service A → Service Registry (Consul, eureka) → gets IP list → calls Service B directly
```

### Server-Side Discovery
```
Service A → Load Balancer (knows about Service B instances) → routes to Service B
```

**In Kubernetes**: Built-in DNS-based discovery
```
# Service B is accessible at:
http://service-b.namespace.svc.cluster.local:8080
```

## API Gateway Pattern

Single entry point for all client requests.

```
Mobile App ──►  ┌───────────┐  ──► User Service
Web App   ──►  │ API Gateway│  ──► Order Service  
3rd Party ──►  └───────────┘  ──► Payment Service
                    │
              - Authentication
              - Rate limiting
              - Request routing
              - Response aggregation
              - Protocol translation
```

**Tools**: Kong, AWS API Gateway, Azure API Management, NGINX, Traefik

### Backend for Frontend (BFF)
Separate API gateways for different clients:
```
Mobile App → Mobile BFF → microservices
Web App    → Web BFF    → microservices
```

## Circuit Breaker Pattern

Prevent cascading failures when a downstream service is down.

```
States:
  CLOSED → (failures exceed threshold) → OPEN → (timeout) → HALF-OPEN
                                          ↑                     │
                                          └─── (test fails) ────┘
                                          
  CLOSED: Normal operation, counting failures
  OPEN: All calls fail immediately (no network call), return fallback
  HALF-OPEN: Allow limited test calls to check if service recovered
```

```python
# Python with tenacity
from tenacity import retry, stop_after_attempt, wait_exponential

@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, max=10)
)
def call_payment_service(order_id):
    response = requests.post(f"{PAYMENT_URL}/charge", json={"order_id": order_id})
    response.raise_for_status()
    return response.json()
```

## Retry and Timeout Strategies

### Exponential Backoff with Jitter

```python
import random

def retry_with_backoff(func, max_retries=3, base_delay=1.0):
    for attempt in range(max_retries):
        try:
            return func()
        except Exception:
            if attempt == max_retries - 1:
                raise
            delay = base_delay * (2 ** attempt)
            jitter = random.uniform(0, delay * 0.1)
            time.sleep(delay + jitter)
```

**Why jitter?** Without it, all retrying clients retry at the same moment → thundering herd.

### Timeout Budget
```
Client timeout: 10s
  → API Gateway: 8s
    → Service A: 5s
      → Service B: 3s (upstream timeout < downstream timeout)
```

## Bulkhead Pattern

Isolate resources so failure in one area doesn't bring down everything.

```
Thread Pool Bulkhead:
  Payment calls: 10 threads max  [████████░░]
  Inventory calls: 5 threads max [███░░]
  Email calls: 3 threads max     [██░]
  
  If Payment service is slow → only those 10 threads block
  Inventory and Email continue working!
```

## Interview Answer

> "For microservices communication, I use gRPC for internal service-to-service calls — it's significantly faster than REST due to HTTP/2 and Protobuf. For external APIs, REST with OpenAPI is more practical. For async communication, I use Kafka for event streaming (ordered, replayable) and SQS for task queues. Resilience patterns are critical: circuit breakers prevent cascading failures, exponential backoff with jitter prevents thundering herds, and timeouts should decrease as you go deeper in the call chain. In Kubernetes, I leverage built-in DNS discovery rather than running a separate service registry."
