# API Gateway Patterns

## What Does an API Gateway Do?
Single entry point that handles cross-cutting concerns for all API traffic.

## Core Responsibilities
1. **Request routing** – Route to appropriate microservice
2. **Authentication/Authorization** – Validate JWT/API keys
3. **Rate limiting** – Protect backend from abuse
4. **Load balancing** – Distribute across instances
5. **Circuit breaking** – Prevent cascade failures
6. **Request/Response transformation** – Schema mapping
7. **Caching** – Reduce backend load
8. **Logging and monitoring** – Centralized observability

## Patterns

### Backend for Frontend (BFF)
```
Mobile App → Mobile BFF Gateway → Microservices
Web App    → Web BFF Gateway    → Microservices
```
Each client type gets its own gateway optimized for it.

### Edge Gateway + Internal Gateway
```
Internet → Edge Gateway (auth, rate limit) → Internal Gateway (routing) → Services
```

## Popular Solutions
| Tool | Type | Best For |
|------|------|----------|
| Kong | Self-hosted | Full-featured, plugin ecosystem |
| AWS API Gateway | Managed | AWS-native, Lambda integration |
| Azure API Management | Managed | Azure-native, policy engine |
| Envoy | Proxy | Service mesh, high performance |
| Traefik | Self-hosted | K8s-native, auto-discovery |
