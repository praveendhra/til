# SLI, SLO, and SLA

## Definitions
- **SLI** (Service Level Indicator): A measurement (e.g., latency, error rate)
- **SLO** (Service Level Objective): A target for an SLI (e.g., 99.9% availability)
- **SLA** (Service Level Agreement): A contract with consequences

## Example
```
SLI: Percentage of requests completing in < 200ms
SLO: 99.5% of requests complete in < 200ms per rolling 30 days
SLA: If SLO is breached, customer gets 10% credit
```

## Common SLIs
| SLI | Measurement |
|-----|------------|
| Availability | Successful requests / total requests |
| Latency | p50, p95, p99 response times |
| Throughput | Requests per second |
| Error rate | Failed requests / total requests |
| Freshness | Age of data served |

## Error Budget
```
SLO: 99.9% availability per month
Error Budget: 0.1% = 43.2 minutes of downtime/month

Remaining budget = 43.2 min - actual downtime
```

If error budget is exhausted:
- Freeze feature releases
- Focus on reliability work
- Post-mortem required for incidents

## Burn Rate Alerts
```
Fast burn: > 14.4x budget consumption rate (page immediately)
Slow burn: > 6x budget consumption rate (ticket)
Very slow: > 1x (monthly report)
```
