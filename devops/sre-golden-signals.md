# SRE Golden Signals

From Google's SRE book — the 4 metrics that matter most:

## 1. Latency
Time to serve a request.
- Distinguish **successful** request latency from **error** latency
- Track p50, p95, p99 (not just average!)
- **SLO example**: p99 latency < 300ms

## 2. Traffic
How much demand is hitting your system.
- HTTP requests/second
- Transactions/second
- Messages consumed/second

## 3. Errors
Rate of failed requests.
- Explicit errors (5xx HTTP status codes)
- Implicit errors (200 response but wrong content)
- **SLO example**: Error rate < 0.1%

## 4. Saturation
How "full" your service is.
- CPU utilization, memory usage
- Queue depth, thread pool usage
- Disk I/O
- **Alert when**: > 80% sustained utilization

## Implementation
```yaml
# Prometheus recording rules example
groups:
- name: golden-signals
  rules:
  - record: job:http_request_duration_seconds:p99
    expr: histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m]))
  - record: job:http_requests_total:rate5m
    expr: rate(http_requests_total[5m])
  - record: job:http_errors:rate5m
    expr: rate(http_requests_total{status=~"5.."}[5m])
```

## RED vs USE vs Golden Signals
- **RED** (Request-focused): Rate, Errors, Duration — for services
- **USE** (Resource-focused): Utilization, Saturation, Errors — for infrastructure
- **Golden Signals**: Superset — covers both perspectives
