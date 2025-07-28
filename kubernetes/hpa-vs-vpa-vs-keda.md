# Kubernetes Autoscaling Options

## HPA (Horizontal Pod Autoscaler)
Scale **number of pods** based on metrics.

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: my-app
  minReplicas: 2
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

## VPA (Vertical Pod Autoscaler)
Adjust **CPU/memory requests** per pod based on actual usage.

- Recommends or auto-applies right-sized requests
- Requires pod restart to apply changes
- Don't use HPA + VPA on same metric (conflict)

## KEDA (Kubernetes Event-Driven Autoscaling)
Scale based on **event sources**: queue depth, Kafka lag, cron, HTTP rate.

```yaml
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
spec:
  scaleTargetRef:
    name: order-processor
  minReplicaCount: 0    # Scale to ZERO!
  maxReplicaCount: 50
  triggers:
  - type: azure-servicebus
    metadata:
      queueName: orders
      messageCount: "5"  # 1 pod per 5 messages
```

## When to Use What
| Autoscaler | Use Case |
|-----------|----------|
| HPA | CPU/memory-based scaling |
| VPA | Right-sizing resource requests |
| KEDA | Event-driven, scale to zero |
| Cluster Autoscaler | Add/remove nodes |
