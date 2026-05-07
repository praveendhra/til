# SRE Golden Signals & Monitoring — Complete Guide

## The Four Golden Signals (Google SRE Book)

| Signal | What It Measures | Example Metrics |
|--------|-----------------|-----------------|
| **Latency** | Time to serve a request | p50, p95, p99 response time |
| **Traffic** | Demand on the system | Requests/sec, concurrent users |
| **Errors** | Rate of failed requests | 5xx rate, error percentage |
| **Saturation** | How full the system is | CPU %, memory %, disk I/O, queue depth |

### Why These Four?

If you can only monitor four things, these tell you:
1. **Latency**: Is the user experience degrading?
2. **Traffic**: Is load normal, growing, or suspicious?
3. **Errors**: Is something broken?
4. **Saturation**: Are we about to break?

## Implementing the Golden Signals

### 1. Latency — USE PERCENTILES, NOT AVERAGES

```
❌ Average response time: 200ms
   (Hides that 5% of users wait 5+ seconds!)

✅ Percentile response times:
   p50:  150ms  (median — half of requests are faster)
   p95:  800ms  (95% of requests are under this)
   p99:  2500ms (99% of requests — the tail)
   p99.9: 8000ms (worst case for most users)
```

**Why percentiles matter**: If you have 1M requests/day and a 1% error rate at p99, that's **10,000 users** having a bad experience.

```yaml
# Prometheus query for latency percentiles
histogram_quantile(0.50, rate(http_request_duration_seconds_bucket[5m]))
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))
histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m]))
```

### 2. Traffic

```yaml
# Requests per second
rate(http_requests_total[5m])

# By endpoint
sum by (endpoint) (rate(http_requests_total[5m]))

# By status code
sum by (status_code) (rate(http_requests_total[5m]))
```

### 3. Errors

```yaml
# Error rate (percentage)
sum(rate(http_requests_total{status=~"5.."}[5m]))
/
sum(rate(http_requests_total[5m]))
* 100

# Error budget burn rate
# If SLO is 99.9% (0.1% error budget per month):
# Monthly error budget = 43.2 minutes of downtime
```

### 4. Saturation

```yaml
# CPU saturation
100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)

# Memory usage
(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100

# Disk I/O utilization
rate(node_disk_io_time_seconds_total[5m]) * 100
```

## USE Method (Brendan Gregg) — For Resources

For every resource (CPU, memory, disk, network), check:
- **U**tilization: Percentage of time busy
- **S**aturation: Queue depth (work waiting)
- **E**rrors: Error count

| Resource | Utilization | Saturation | Errors |
|----------|-------------|-----------|--------|
| CPU | `cpu_usage_percent` | `load_average / num_cores` | Machine check exceptions |
| Memory | `used / total` | Swap usage, OOM events | ECC errors |
| Disk | `io_time_percent` | `avgqu-sz` (avg queue size) | Read/write errors |
| Network | Bandwidth usage | Dropped packets, retransmits | CRC errors |

## RED Method (Tom Wilkie) — For Services

For every service, track:
- **R**ate: Requests per second
- **E**rrors: Number of failed requests
- **D**uration: Time per request (histogram)

## SLI, SLO, SLA Hierarchy

```
SLA (Service Level Agreement)
  ├── Business contract with consequences
  │   "99.9% uptime or we refund 10% of monthly fee"
  │
  └── Based on SLOs:

SLO (Service Level Objective)
  ├── Internal target, stricter than SLA
  │   "99.95% of requests return successfully within 500ms"
  │
  └── Measured by SLIs:

SLI (Service Level Indicator)
  ├── Actual measurement
  │   "Proportion of requests completing < 500ms over 30-day window"
  │   Current SLI: 99.97%
  └── 
```

### Error Budget

```
SLO: 99.9% availability per month

Error Budget = 100% - 99.9% = 0.1%
In minutes: 30 days × 24h × 60m × 0.001 = 43.2 minutes/month

If you've used 30 minutes already this month:
  Remaining: 13.2 minutes
  Burn rate: Higher than expected → slow down deployments!
```

## Alerting Best Practices

### Alert on Symptoms, Not Causes

```yaml
# ❌ Bad: Alert on cause (CPU high)
- alert: HighCPU
  expr: cpu_usage > 90
  # Problem: High CPU might be fine (batch job)

# ✅ Good: Alert on symptom (users affected)
- alert: HighErrorRate
  expr: |
    sum(rate(http_requests_total{status=~"5.."}[5m]))
    / sum(rate(http_requests_total[5m])) > 0.01
  for: 5m
  labels:
    severity: critical
  annotations:
    summary: "Error rate above 1% for 5 minutes"
```

### Multi-Window, Multi-Burn-Rate Alerts (Google SRE)

```yaml
# Fast burn: 2% of monthly budget in 1 hour
- alert: ErrorBudgetFastBurn
  expr: error_ratio > 14.4 * 0.001  # 14.4x burn rate
  for: 2m
  labels:
    severity: page  # Wake someone up

# Slow burn: 5% of monthly budget in 6 hours
- alert: ErrorBudgetSlowBurn
  expr: error_ratio > 6 * 0.001  # 6x burn rate
  for: 15m
  labels:
    severity: ticket  # Create a ticket, don't page
```

## Monitoring Stack

```
┌──────────┐     ┌────────────┐     ┌──────────┐     ┌────────────┐
│ App      │────▶│ Prometheus │────▶│ Grafana  │     │ PagerDuty/ │
│ (metrics)│     │ (scrape &  │     │ (dashboards)    │ OpsGenie   │
└──────────┘     │  store)    │────▶│          │────▶│ (alerting) │
                 └────────────┘     └──────────┘     └────────────┘
                       │
                 ┌─────▼──────┐
                 │ AlertManager│
                 │ (routing,   │
                 │  dedup,     │
                 │  silencing) │
                 └─────────────┘
```

## Interview Answer

> "I monitor services using the four golden signals: latency (percentiles, not averages), traffic, errors, and saturation. For alerting, I follow the SRE principle of alerting on symptoms — high error rate or elevated latency — rather than causes like high CPU, which might be expected behavior. I define SLOs for each service (e.g., 99.9% of requests under 500ms) and track error budget consumption. For alerting, I use multi-burn-rate alerts: a fast burn (paging) catches acute incidents, while a slow burn (ticket) catches gradual degradation. The stack is typically Prometheus + Grafana + AlertManager, with PagerDuty for escalation."
