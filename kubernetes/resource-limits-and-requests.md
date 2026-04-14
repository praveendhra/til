# Kubernetes Resource Requests vs Limits — Critical Concepts

## Requests vs Limits

| Concept | Purpose | Scheduling | Enforcement |
|---------|---------|-----------|-------------|
| **Request** | Guaranteed minimum resources | Used by scheduler for placement | Always available to the pod |
| **Limit** | Maximum resources allowed | Not used for scheduling | Enforced by kubelet/kernel |

```yaml
resources:
  requests:
    cpu: 250m      # Guaranteed 0.25 CPU cores
    memory: 256Mi  # Guaranteed 256 MiB
  limits:
    cpu: 500m      # Can burst up to 0.5 CPU cores
    memory: 512Mi  # Hard cap — OOMKilled if exceeded
```

## CPU vs Memory: Critical Difference

### CPU: Compressible (Throttled)
- Exceeding CPU limit → **throttled** (slowed down, not killed)
- CPU is time-shared: 250m = 25% of one core
- Pod keeps running, just slower

### Memory: Incompressible (OOMKilled)
- Exceeding memory limit → **OOMKilled** (process terminated, exit code 137)
- No way to "take back" allocated memory
- Container restarts (CrashLoopBackOff if repeated)

```
CPU over limit:     Pod runs slow (throttled) — annoying but alive
Memory over limit:  Pod gets KILLED (OOMKilled) — restarts
```

## QoS Classes

Kubernetes assigns QoS classes based on how requests/limits are set:

| QoS Class | Condition | Eviction Priority |
|-----------|-----------|-------------------|
| **Guaranteed** | requests == limits (for all containers) | Last to be evicted |
| **Burstable** | requests < limits (at least one set) | Evicted after BestEffort |
| **BestEffort** | No requests or limits set | First to be evicted |

```yaml
# Guaranteed (most protection)
resources:
  requests:
    cpu: 500m
    memory: 512Mi
  limits:
    cpu: 500m       # Same as request
    memory: 512Mi   # Same as request

# Burstable (common for most workloads)
resources:
  requests:
    cpu: 250m
    memory: 256Mi
  limits:
    cpu: 500m       # Can burst
    memory: 512Mi

# BestEffort (no resources set — avoid in production!)
# No resources block at all
```

**Production rule**: Always set at least requests. Use Guaranteed QoS for critical workloads (databases, payment services).

## CPU Units Explained

```
1 CPU = 1 vCPU (AWS) = 1 Core (GCP) = 1 vCore (Azure) = 1 Hyperthread

1000m (millicores) = 1 CPU
500m = 0.5 CPU = half a core
250m = 0.25 CPU = quarter core
100m = 0.1 CPU = 10% of a core

Example: A 4-core node has 4000m of allocatable CPU
```

## Memory Units

```
Ki = kibibyte = 1024 bytes
Mi = mebibyte = 1024 Ki = 1,048,576 bytes
Gi = gibibyte = 1024 Mi

K  = kilobyte = 1000 bytes (avoid — use Ki)
M  = megabyte = 1,000,000 bytes (avoid — use Mi)
G  = gigabyte = 1,000,000,000 bytes

128Mi ≈ 134 MB
256Mi ≈ 268 MB
1Gi   ≈ 1.07 GB
```

## Common Mistakes

### 1. Not Setting Requests (Worst Practice)
```yaml
# ❌ No requests = BestEffort QoS = first to die under pressure
containers:
  - name: app
    image: myapp
    # No resources block!
```

### 2. Request Much Lower Than Actual Usage
```yaml
# ❌ Request 100m but app uses 400m regularly
# Scheduler thinks it has room, but node is overcommitted
resources:
  requests:
    cpu: 100m    # Way too low
```
Results in CPU throttling and poor performance across the node.

### 3. Setting CPU Limits (Controversial!)
Some teams argue **don't set CPU limits**:
- CPU limits cause **throttling even when the node has spare CPU**
- A pod requesting 250m with no limit can burst to use idle CPU
- Google's recommendation: Set CPU requests, not limits

```yaml
# Google/Datadog recommendation:
resources:
  requests:
    cpu: 250m
    memory: 256Mi
  limits:
    # cpu: intentionally not set — allow bursting
    memory: 512Mi  # Always set memory limits!
```

### 4. Memory Limit Too Close to Request
```yaml
# ❌ Too tight — OOMKilled on any spike
resources:
  requests:
    memory: 256Mi
  limits:
    memory: 260Mi  # Only 4Mi headroom!

# ✅ Better — 2x headroom
resources:
  requests:
    memory: 256Mi
  limits:
    memory: 512Mi
```

## How to Right-Size Resources

```bash
# 1. Check actual usage (requires metrics-server)
kubectl top pods -n production --sort-by=cpu
kubectl top pods -n production --sort-by=memory

# 2. Use VPA recommendations
kubectl get vpa -n production -o yaml

# 3. Check Prometheus metrics for trends
# container_cpu_usage_seconds_total
# container_memory_working_set_bytes

# 4. General formula:
#   Request = P95 of actual usage
#   Limit = P99 of actual usage × 1.5 safety margin
```

## LimitRange — Namespace Defaults

```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: default-limits
  namespace: production
spec:
  limits:
    - type: Container
      default:         # Default limits (if not specified)
        cpu: 500m
        memory: 512Mi
      defaultRequest:  # Default requests (if not specified)
        cpu: 100m
        memory: 128Mi
      max:             # Maximum limits allowed
        cpu: 2
        memory: 4Gi
      min:             # Minimum requests allowed
        cpu: 50m
        memory: 64Mi
```

## ResourceQuota — Namespace Budget

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: production-quota
  namespace: production
spec:
  hard:
    requests.cpu: "20"        # Total CPU requests in namespace
    requests.memory: 40Gi
    limits.cpu: "40"
    limits.memory: 80Gi
    pods: "100"               # Max pods in namespace
    persistentvolumeclaims: "20"
```

## Interview Answer

> "Requests are the guaranteed minimum resources used by the scheduler for placement, while limits are hard caps enforced at runtime. The critical difference is that CPU is compressible — exceeding limits just throttles the pod — but memory is incompressible — exceeding limits kills the pod with OOMKilled. I always set memory limits to prevent runaway processes, but I'm careful with CPU limits because they cause throttling even when the node has spare capacity. For right-sizing, I use VPA in recommendation mode and Prometheus metrics to set requests at P95 actual usage."
