# Observability — Structured Logging

## Why Structured Logs

Plain text logs:
```
2026-06-15 10:23:45 ERROR Failed to process order 12345 for user abc
```

Structured logs (JSON):
```json
{
  "timestamp": "2026-06-15T10:23:45Z",
  "level": "error",
  "message": "Failed to process order",
  "order_id": "12345",
  "user_id": "abc",
  "error": "payment_declined",
  "duration_ms": 230,
  "trace_id": "4bf92f3577b34da6"
}
```

Structured logs are **searchable, filterable, and aggregatable** in log platforms.

## Python — structlog

```python
import structlog

structlog.configure(
    processors=[
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.add_log_level,
        structlog.processors.StackInfoRenderer(),
        structlog.processors.JSONRenderer(),
    ]
)

log = structlog.get_logger()

# Bind context that persists across log calls
log = log.bind(request_id="req-123", user_id="abc")
log.info("processing_order", order_id="12345")
log.error("order_failed", order_id="12345", error="payment_declined")
```

## Context Propagation

```python
# Middleware adds request context once
@app.middleware("http")
async def logging_middleware(request, call_next):
    structlog.contextvars.clear_contextvars()
    structlog.contextvars.bind_contextvars(
        request_id=request.headers.get("x-request-id", str(uuid4())),
        path=request.url.path,
        method=request.method,
    )
    response = await call_next(request)
    return response

# All logs in this request automatically include request_id, path, method
```

## Log Levels — When to Use What

| Level | Use Case |
|-------|----------|
| DEBUG | Detailed internal state (disabled in prod) |
| INFO | Normal operations: request served, job completed |
| WARN | Recoverable issues: retry succeeded, fallback used |
| ERROR | Failed operations that need attention |
| FATAL | App cannot continue, shutting down |

## Best Practices

- **Log at boundaries**: HTTP handlers, queue consumers, job start/end
- **Include identifiers**: request_id, trace_id, user_id, order_id
- **Log outcomes, not implementations**: "order_created" not "inserted into postgres"
- **Use consistent field names**: `duration_ms` everywhere, not sometimes `elapsed`
- **Don't log sensitive data**: Mask PII, never log passwords or tokens
- **Correlation**: Include `trace_id` to link logs with distributed traces

## Querying (Datadog/Loki/CloudWatch)

```
# Datadog
service:orders @level:error @order_id:12345

# Grafana Loki (LogQL)
{service="orders"} | json | level="error" | order_id="12345"

# CloudWatch Insights
fields @timestamp, message, order_id
| filter level = "error"
| sort @timestamp desc
```
