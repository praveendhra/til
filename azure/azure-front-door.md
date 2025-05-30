# Azure Front Door

## What Is It?
Global load balancer and CDN that provides fast, reliable, and secure access to web applications.

## Key Features
- **Global HTTP load balancing** with instant failover
- **SSL offloading** at the edge
- **WAF (Web Application Firewall)** integration
- **URL-based routing** and rewrites
- **Session affinity** (sticky sessions)
- **Health probes** for backend monitoring

## Routing Architecture
```
User → Azure Front Door (edge POP) → Backend Pool
                                     ├── App Service (East US)
                                     ├── App Service (West Europe)
                                     └── AKS (Southeast Asia)
```

## Routing Methods
1. **Latency** – Route to lowest-latency backend
2. **Priority** – Primary/secondary backend failover
3. **Weighted** – Distribute traffic by percentage
4. **Session Affinity** – Same user → same backend

## vs Application Gateway
| Feature | Front Door | App Gateway |
|---------|-----------|-------------|
| Scope | Global | Regional |
| Layer | L7 (HTTP) | L7 (HTTP) |
| CDN | Built-in | No |
| WAF | Yes | Yes |
| Use case | Multi-region | Single-region |
