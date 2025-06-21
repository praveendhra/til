# Azure Container Apps

## What Is It?
Serverless container platform built on Kubernetes (but you don't manage K8s).

## Key Features
- **Auto-scaling** including scale-to-zero
- **Dapr integration** for microservices patterns
- **Revision management** for blue/green deployments
- **Built-in ingress** with HTTPS

## When to Use (vs other Azure compute)
| Service | Use When |
|---------|----------|
| Container Apps | Microservices, event-driven, APIs |
| App Service | Traditional web apps |
| AKS | Need full K8s control |
| Container Instances | Simple single-container tasks |
| Functions | Short-lived event handlers |

## Scaling Rules
```yaml
scale:
  minReplicas: 0
  maxReplicas: 30
  rules:
    - name: http-scaling
      http:
        metadata:
          concurrentRequests: "50"
    - name: queue-scaling
      azureQueue:
        queueName: orders
        queueLength: 10
```

## Dapr Integration
Built-in sidecar for:
- Service-to-service invocation
- State management
- Pub/sub messaging
- Input/output bindings
