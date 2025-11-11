# Kubernetes Resource Requests vs Limits

## Requests
**Guaranteed** resources. Scheduler uses this to place pods.
```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"    # 0.25 vCPU
```

## Limits
**Maximum** resources. Exceeding causes throttling (CPU) or OOMKill (memory).
```yaml
resources:
  limits:
    memory: "512Mi"
    cpu: "500m"
```

## What Happens When...
| Scenario | CPU | Memory |
|----------|-----|--------|
| Exceed request | Runs fine (if node has capacity) | Runs fine |
| Exceed limit | Throttled (slowed down) | OOMKilled |
| No request set | Defaults to limit (or 0) | Defaults to limit (or 0) |
| No limit set | Uses all available CPU | Could OOMKill node |

## Best Practices
1. **Always set requests** — prevents noisy neighbor problems
2. **Set memory limits** — prevent OOMKill at node level
3. **Be cautious with CPU limits** — throttling causes latency spikes
4. **Use VPA** (Vertical Pod Autoscaler) to right-size over time

## QoS Classes
| Class | Condition | Priority |
|-------|-----------|----------|
| Guaranteed | requests == limits (all containers) | Highest |
| Burstable | requests < limits | Medium |
| BestEffort | No requests or limits | Lowest (evicted first) |

## Practical Tip
Start with generous limits, use metrics to right-size:
```bash
kubectl top pods --sort-by=memory
```
