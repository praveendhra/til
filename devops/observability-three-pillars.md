# Three Pillars of Observability

## 1. Metrics (What happened?)
Numeric measurements aggregated over time.

```
http_requests_total{method="GET", status="200"} 1234
http_request_duration_seconds{quantile="0.99"} 0.25
```

**Tools**: Prometheus, Datadog, CloudWatch
**Use for**: Dashboards, alerting, SLOs

## 2. Logs (Why did it happen?)
Timestamped text records of discrete events.

```json
{"timestamp": "2025-03-15T10:30:00Z", "level": "ERROR",
 "message": "Connection refused", "service": "payment",
 "trace_id": "abc123"}
```

**Tools**: ELK Stack, Loki, CloudWatch Logs
**Use for**: Debugging, audit trail, root cause analysis

## 3. Traces (Where did it happen?)
End-to-end request flow across services.

```
[Trace abc123]
├── API Gateway (2ms)
├── Auth Service (5ms)
├── Order Service (150ms)
│   ├── Database Query (80ms)  ← bottleneck
│   └── Cache Lookup (2ms)
└── Notification Service (10ms)
```

**Tools**: Jaeger, Zipkin, AWS X-Ray, Datadog APT
**Use for**: Latency analysis, dependency mapping

## OpenTelemetry
Unified standard for all three pillars:
```python
from opentelemetry import trace, metrics
tracer = trace.get_tracer("myapp")
meter = metrics.get_meter("myapp")

with tracer.start_as_current_span("process_order") as span:
    span.set_attribute("order.id", order_id)
    counter.add(1, {"method": "POST"})
```
