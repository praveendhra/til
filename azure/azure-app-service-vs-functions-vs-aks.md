# Azure App Service vs Functions vs AKS

## Azure Functions (Serverless)
- Event-driven, auto-scale to zero
- **Consumption plan**: Pay per execution, 5-min timeout
- **Premium plan**: Pre-warmed, VNet integration, 60-min timeout
- **Triggers**: HTTP, Timer, Blob, Queue, Event Hub, Cosmos DB change feed
- Best for: Event-driven processing, lightweight APIs

## Azure App Service (PaaS)
- Managed web hosting for apps (ASP.NET, Java, Node, Python)
- Always-on, deployment slots, auto-scale
- Built-in CI/CD, custom domains, SSL
- **Pricing**: Per App Service Plan (shared compute)
- Best for: Web apps, REST APIs, traditional architectures

## Azure Kubernetes Service (AKS)
- Managed Kubernetes, free control plane
- **Node pools**: System + user, spot instances
- **KEDA**: Event-driven autoscaling for K8s
- **Dapr**: Distributed application runtime (sidecars)
- Best for: Complex microservices, multi-container, K8s expertise

## Azure Container Apps
- Serverless containers (built on Kubernetes + KEDA + Dapr)
- Scale to zero, event-driven
- Simpler than AKS, more powerful than Functions
- Best for: Microservices without K8s complexity

## Decision Guide
```
Simple function/webhook? → Azure Functions
Web app/API? → App Service
Microservices, need K8s? → AKS
Microservices, want simplicity? → Container Apps
```
