# Three Pillars of Observability

## 1. Logs
Discrete events with context. Structured logging (JSON) > unstructured.

```json
{
  "timestamp": "2025-03-15T10:30:00Z",
  "level": "ERROR",
  "service": "payment-api",
  "trace_id": "abc123",
  "message": "Payment failed",
  "user_id": "usr_456",
  "error": "insufficient_funds"
}
```

**Cloud**: CloudWatch Logs, Azure Monitor Logs, Cloud Logging

## 2. Metrics
Numeric measurements over time. Counters, gauges, histograms.

- **RED Method** (for services): Rate, Errors, Duration
- **USE Method** (for resources): Utilization, Saturation, Errors

**Cloud**: CloudWatch Metrics, Azure Monitor Metrics, Cloud Monitoring

## 3. Traces
End-to-end request flow across services.

```
[API Gateway: 200ms]
  └─[Auth Service: 15ms]
  └─[Order Service: 150ms]
      └─[DB Query: 80ms]
      └─[Payment Service: 50ms]
```

**Cloud**: X-Ray (AWS), Application Insights (Azure), Cloud Trace (GCP)

## OpenTelemetry
Vendor-neutral standard for all three pillars. Use OTEL SDK → export to any backend.

## SLIs, SLOs, SLAs
- **SLI** (Indicator): The metric (e.g., latency p99 = 200ms)
- **SLO** (Objective): The target (e.g., p99 latency < 300ms, 99.9% of time)
- **SLA** (Agreement): The contract with consequences (e.g., refund if SLO breached)
