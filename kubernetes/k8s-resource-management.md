# Kubernetes Resource Management

## Requests vs Limits
```yaml
resources:
  requests:    # Guaranteed resources (scheduling)
    cpu: 250m     # 0.25 CPU cores
    memory: 256Mi
  limits:      # Maximum allowed (enforcement)
    cpu: 500m
    memory: 512Mi
```

## What Happens When Limits Are Exceeded?
- **CPU**: Throttled (container slowed down)
- **Memory**: OOMKilled (container restarted)

## QoS Classes
| Class | Condition | Priority |
|-------|-----------|----------|
| Guaranteed | requests == limits for all containers | Highest |
| Burstable | requests < limits for any container | Medium |
| BestEffort | No requests or limits set | Lowest (first to evict) |

## LimitRange (namespace defaults)
```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: default-limits
spec:
  limits:
    - default:        # Default limits
        cpu: 500m
        memory: 512Mi
      defaultRequest:  # Default requests
        cpu: 100m
        memory: 128Mi
      type: Container
```

## ResourceQuota (namespace totals)
```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: compute-quota
spec:
  hard:
    requests.cpu: "10"
    requests.memory: 20Gi
    limits.cpu: "20"
    limits.memory: 40Gi
    pods: "50"
```

## Sizing Guidelines
- Start with requests = average usage
- Set limits = 2x requests (or peak observed)
- Use VPA (Vertical Pod Autoscaler) for recommendations
