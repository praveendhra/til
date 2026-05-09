# Deployment Strategies — Blue-Green, Canary, Rolling

## Overview

| Strategy | Risk | Speed | Rollback | Cost | Complexity |
|----------|------|-------|----------|------|------------|
| **Recreate** | 🔴 High (downtime) | Fast | Redeploy old | 1x | Very Low |
| **Rolling** | 🟡 Medium | Moderate | Rolling back | 1x | Low |
| **Blue-Green** | 🟢 Low | Instant switch | Instant | 2x | Medium |
| **Canary** | 🟢 Very Low | Slow (gradual) | Instant | 1.x | High |
| **A/B Testing** | 🟢 Low | Slow | Instant | 1.x | Very High |

## Recreate (Big Bang)

```
v1 v1 v1  →  (downtime)  →  v2 v2 v2
███████    →  ░░░░░░░░░░  →  ███████
```

- Stop all old pods, start all new pods
- Simple but has **downtime** during transition
- Only use for: Dev/staging, or when downtime is acceptable

```yaml
# Kubernetes
spec:
  strategy:
    type: Recreate
```

## Rolling Update (Default in K8s)

```
Time →
v1 v1 v1 v1    Start: all v1
v1 v1 v1 v2    Replace one at a time
v1 v1 v2 v2    ...
v1 v2 v2 v2    ...
v2 v2 v2 v2    Done: all v2
```

- Replace pods incrementally
- Always some pods serving traffic
- **Zero downtime** but both versions run simultaneously

```yaml
# Kubernetes
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1    # Max pods that can be down during update
      maxSurge: 1          # Max extra pods created during update
  minReadySeconds: 30      # Wait 30s after pod is Ready before continuing
```

**Pros**: Simple, built into K8s, zero downtime
**Cons**: Slow rollback (must roll forward or back through each pod), both versions serve traffic during rollout, harder to test full deployment before switching

## Blue-Green Deployment

```
                    Load Balancer
                         │
              ┌──────────┼──────────┐
              │                     │
        ┌─────▼─────┐        ┌─────▼─────┐
        │  BLUE     │        │  GREEN    │
        │  (v1)     │        │  (v2)     │
        │  ACTIVE   │        │  IDLE     │
        └───────────┘        └───────────┘

Step 1: Deploy v2 to GREEN (blue still serving traffic)
Step 2: Test GREEN thoroughly
Step 3: Switch LB to GREEN ← instant cutover
Step 4: BLUE becomes standby (instant rollback if needed)
```

```yaml
# Kubernetes: Use label selector switch
# Service pointing to v1:
apiVersion: v1
kind: Service
metadata:
  name: my-app
spec:
  selector:
    app: my-app
    version: v1    # ← Change to v2 for instant switch

# Both deployments exist:
# Deployment: my-app-v1 (labels: version=v1)
# Deployment: my-app-v2 (labels: version=v2)
```

**Pros**: Instant switchover, instant rollback, full testing before switch
**Cons**: **Requires 2x infrastructure** during deployment, database migrations must be backward-compatible

### Database Considerations for Blue-Green

```
Problem: v1 and v2 might need different database schemas

Solution: Expand-and-Contract pattern
  1. Expand: Add new columns/tables (backward compatible with v1)
  2. Deploy v2 (uses new columns)
  3. Switch traffic to v2
  4. Contract: Remove old columns (after v1 is decommissioned)
```

## Canary Deployment

```
Step 1: Deploy v2 to small percentage
                    Load Balancer
                    ┌────┤────┐
                    │ 95%│ 5% │
              ┌─────▼────┐ ┌──▼───┐
              │ v1 (95%) │ │ v2   │ ← canary
              │ █████████│ │ █    │
              └──────────┘ └──────┘

Step 2: Monitor error rate, latency, business metrics
Step 3: Gradually increase: 5% → 10% → 25% → 50% → 100%
Step 4: If problems detected at any stage → rollback to 0%
```

### Canary with Istio

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: my-app
spec:
  http:
    - route:
        - destination:
            host: my-app
            subset: v1
          weight: 95
        - destination:
            host: my-app
            subset: v2
          weight: 5     # 5% canary

---
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: my-app
spec:
  host: my-app
  subsets:
    - name: v1
      labels:
        version: v1
    - name: v2
      labels:
        version: v2
```

### Automated Canary with Flagger

```yaml
apiVersion: flagger.app/v1beta1
kind: Canary
metadata:
  name: my-app
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: my-app
  progressDeadlineSeconds: 600
  analysis:
    interval: 1m
    threshold: 5          # Max failed checks before rollback
    maxWeight: 50         # Max traffic to canary
    stepWeight: 10        # Increase 10% per interval
    metrics:
      - name: request-success-rate
        thresholdRange:
          min: 99          # Rollback if success rate < 99%
      - name: request-duration
        thresholdRange:
          max: 500         # Rollback if p99 > 500ms
```

## GitOps with ArgoCD Rollouts

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: my-app
spec:
  strategy:
    canary:
      steps:
        - setWeight: 5
        - pause: {duration: 5m}     # Wait 5 min, monitor
        - setWeight: 20
        - pause: {duration: 5m}
        - setWeight: 50
        - pause: {duration: 10m}
        - setWeight: 100
      canaryService: my-app-canary
      stableService: my-app-stable
      trafficRouting:
        nginx:
          stableIngress: my-app-ingress
```

## Decision Guide

```
How critical is the service?
  │
  ├─ Non-critical (internal tools, staging)
  │   └─ Rolling Update (simplest)
  │
  ├─ Critical (customer-facing, revenue-impacting)
  │   ├─ Need instant rollback?
  │   │   └─ Blue-Green
  │   └─ Need gradual validation?
  │       └─ Canary (with automated analysis)
  │
  └─ Very critical (payments, auth)
      └─ Canary + Manual approval gates
```

## Interview Answer

> "For most services, I use rolling updates in Kubernetes — they're built-in and provide zero-downtime deployments. For critical services, I implement canary deployments using Argo Rollouts or Flagger, which gradually shift traffic (5% → 20% → 50% → 100%) while monitoring error rates and latency. If metrics degrade, it automatically rolls back. Blue-green is useful when you need to test the full deployment before switching and want instant rollback, but it requires double the infrastructure. The key is automating the promotion criteria — don't rely on humans watching dashboards."
