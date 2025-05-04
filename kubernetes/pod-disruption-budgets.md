# Pod Disruption Budgets (PDB)

## Problem
Node drain during upgrade/maintenance evicts ALL pods on that node. If all replicas are on the same node, your app goes down.

## Solution
PDB ensures minimum availability during voluntary disruptions.

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: my-app-pdb
spec:
  minAvailable: 2          # At least 2 pods must stay running
  selector:
    matchLabels:
      app: my-app
```

Or use maxUnavailable:
```yaml
spec:
  maxUnavailable: 1        # At most 1 pod can be down at a time
```

## When PDB Applies
- `kubectl drain` (node maintenance)
- Cluster autoscaler removing nodes
- Node upgrades (GKE, EKS, AKS auto-upgrades)

## When PDB Does NOT Apply
- Pod crashes (involuntary disruption)
- Node failure
- `kubectl delete pod`

## Best Practices
1. Always create PDBs for production deployments
2. Use `maxUnavailable: 1` or percentage-based
3. Combine with **pod anti-affinity** to spread across nodes
4. Set `topologySpreadConstraints` for zone-level spread

```yaml
# Pod anti-affinity example
affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 100
      podAffinityTerm:
        labelSelector:
          matchLabels:
            app: my-app
        topologyKey: kubernetes.io/hostname
```
