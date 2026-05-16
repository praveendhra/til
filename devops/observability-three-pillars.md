# Observability — Logs, Metrics, Traces

## The Three Pillars

```
┌─────────────────────────────────────────────────────────┐
│                    Observability                        │
│                                                         │
│  ┌───────────┐    ┌───────────┐    ┌───────────┐       │
│  │  Metrics  │    │   Logs    │    │  Traces   │       │
│  │           │    │           │    │           │       │
│  │ Aggregated│    │ Detailed  │    │ Request   │       │
│  │ numbers   │    │ events    │    │ journey   │       │
│  │ over time │    │           │    │ across    │       │
│  │           │    │           │    │ services  │       │
│  │ "What is  │    │ "Why did  │    │ "Where is │       │
│  │  broken?" │    │  it       │    │  the      │       │
│  │           │    │  break?"  │    │  slowness?"       │
│  └───────────┘    └───────────┘    └───────────┘       │
└─────────────────────────────────────────────────────────┘
```

## Metrics — The Numbers

Time-series data: numerical values over time.

### Types of Metrics

| Type | Description | Example |
|------|------------|---------|
| **Counter** | Monotonically increasing | Total requests, total errors |
| **Gauge** | Can go up or down | CPU usage, queue depth, temperature |
| **Histogram** | Distribution of values | Request latency buckets |
| **Summary** | Pre-calculated quantiles | p50, p95, p99 latency |

### Prometheus Metric Types

```python
from prometheus_client import Counter, Gauge, Histogram

# Counter: total requests served
REQUEST_COUNT = Counter('http_requests_total', 'Total HTTP requests',
                        ['method', 'endpoint', 'status'])

# Gauge: current active connections
ACTIVE_CONNECTIONS = Gauge('active_connections', 'Active connections')

# Histogram: request duration
REQUEST_DURATION = Histogram('http_request_duration_seconds',
                             'HTTP request duration',
                             buckets=[0.01, 0.05, 0.1, 0.25, 0.5, 1, 5, 10])
```

### Metric Naming Conventions
```
# Prometheus convention:
<namespace>_<subsystem>_<name>_<unit>

# Examples:
http_requests_total                    # Counter (suffix: _total)
http_request_duration_seconds          # Histogram (suffix: _seconds)
node_memory_usage_bytes                # Gauge (suffix: _bytes)
process_open_fds                       # Gauge (unit-less)
```

### Tools
- **Prometheus** (pull-based, K8s native)
- **Datadog** (SaaS, push-based)
- **CloudWatch** (AWS native)
- **Azure Monitor** (Azure native)
- **Grafana** (visualization for any metric source)

## Logs — The Events

Detailed records of discrete events.

### Structured Logging (Always Use This)

```python
import structlog
import logging

logger = structlog.get_logger()

# ❌ Unstructured (hard to parse, search, alert on)
logging.info(f"User {user_id} placed order {order_id} for ${amount}")

# ✅ Structured (searchable, parseable, filterable)
logger.info("order_placed",
    user_id=user_id,
    order_id=order_id,
    amount=amount,
    payment_method="credit_card",
    items_count=3)

# Output (JSON):
# {"event": "order_placed", "user_id": "u123", "order_id": "ord456",
#  "amount": 49.99, "payment_method": "credit_card", "items_count": 3,
#  "timestamp": "2025-01-15T10:30:00Z", "level": "info"}
```

### Log Levels (Use Correctly!)

| Level | When to Use | Alertable? |
|-------|------------|------------|
| **DEBUG** | Detailed diagnostic info (not in prod) | No |
| **INFO** | Normal operations, business events | No |
| **WARN** | Unexpected but recoverable situations | Monitor |
| **ERROR** | Failed operations, exceptions | Yes |
| **FATAL** | System cannot continue | Page immediately |

### Log Aggregation Stack

```
Application → Fluentd/Fluent Bit → Elasticsearch/Loki → Kibana/Grafana
              (collection)         (storage/indexing)    (search/visualize)
```

**Popular stacks**:
- **ELK**: Elasticsearch + Logstash + Kibana
- **EFK**: Elasticsearch + Fluentd + Kibana (K8s-friendly)
- **PLG**: Promtail + Loki + Grafana (lightweight, label-based)
- **Datadog**: SaaS (logs + metrics + traces in one)

### Loki vs Elasticsearch

| Feature | Loki | Elasticsearch |
|---------|------|--------------|
| Indexing | Labels only (like Prometheus) | Full-text indexing |
| Storage cost | Much cheaper | Expensive (indexes everything) |
| Query language | LogQL | KQL / Lucene |
| Search speed | Slower for ad-hoc text search | Fast full-text search |
| Best for | K8s-native, cost-conscious | Complex log analysis |

## Traces — The Request Journey

Follow a single request across multiple services.

```
Client → API Gateway → User Service → Database
                    → Order Service → Payment API
                                   → Inventory Service

Trace ID: abc-123
├── Span 1: API Gateway (2ms)
├── Span 2: User Service (15ms)
│   └── Span 3: Database query (8ms)
├── Span 4: Order Service (45ms)
│   ├── Span 5: Payment API call (30ms)
│   └── Span 6: Inventory check (10ms)
Total: 62ms (but user sees 47ms due to parallelism)
```

### OpenTelemetry (OTel) — The Standard

```python
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter

# Setup
provider = TracerProvider()
provider.add_span_processor(BatchSpanProcessor(OTLPSpanExporter()))
trace.set_tracer_provider(provider)
tracer = trace.get_tracer("order-service")

# Create spans
@tracer.start_as_current_span("process_order")
def process_order(order_id):
    with tracer.start_as_current_span("validate_payment") as span:
        span.set_attribute("order.id", order_id)
        span.set_attribute("payment.method", "credit_card")
        result = payment_service.validate(order_id)
        if not result.success:
            span.set_status(StatusCode.ERROR)
        return result
```

### Trace Context Propagation

```
Service A                         Service B
┌───────────────────┐            ┌───────────────────┐
│ traceparent:      │ ──HTTP──►  │ Read traceparent   │
│ 00-{trace_id}-    │            │ Continue trace     │
│ {span_id}-01      │            │ Create child span  │
└───────────────────┘            └───────────────────┘

# W3C Trace Context header:
traceparent: 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01
```

### Tracing Tools
- **Jaeger** (open source, CNCF)
- **Zipkin** (open source, Twitter)
- **Tempo** (Grafana, works with Loki)
- **AWS X-Ray**
- **Datadog APM**
- **Azure Application Insights**

## Correlation: Tying It All Together

```json
// Every log, metric, and trace should share:
{
  "trace_id": "abc-123",          // Links to distributed trace
  "span_id": "def-456",           // Specific operation
  "request_id": "req-789",        // Business-level correlation
  "service": "order-service",     // Which service
  "environment": "production",    // Which environment
  "user_id": "u123"              // Who was affected
}
```

With correlation IDs, you can jump from:
- Alert → Dashboard → Logs → Trace → Root cause

## Interview Answer

> "Observability requires metrics, logs, and traces working together. Metrics tell me what's broken (high error rate, elevated latency). Traces show me where in the request flow the problem is (which service, which call). Logs tell me why it broke (stack traces, error messages). I use OpenTelemetry as the standard instrumentation library — it handles all three signals and is vendor-neutral. The key is correlating everything with trace IDs so I can jump from a Grafana alert to Jaeger traces to structured logs in Loki, all for the same request. Always use structured logging — JSON with consistent fields — so logs are searchable and parseable."
