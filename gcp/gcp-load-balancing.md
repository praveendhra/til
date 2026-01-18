# GCP Load Balancing

## Global Load Balancers
**Single anycast IP** for worldwide traffic.

### External HTTPS LB
- L7, global, anycast IP
- SSL termination, URL-based routing
- Cloud CDN integration, Cloud Armor WAF
- Backends: GCE, GKE, Cloud Run, Cloud Functions
- **Most common for public web apps**

### External TCP/UDP Proxy LB
- L4, global, for non-HTTP traffic
- SSL offloading for TCP

## Regional Load Balancers

### Internal TCP/UDP LB
- L4, regional, for internal traffic
- VM-to-VM communication within VPC
- Passthrough (DSR) — high performance

### Internal HTTPS LB (Envoy-based)
- L7, regional, internal
- URL-based routing for microservices
- gRPC support

## Serverless NEG (Network Endpoint Group)
Route traffic to serverless services:
```
HTTPS LB → Serverless NEG → Cloud Run
                           → Cloud Functions
                           → App Engine
```

## Comparison
| Feature | GCP HTTPS LB | AWS ALB | Azure Front Door |
|---------|-------------|---------|-----------------|
| Scope | Global | Regional | Global |
| Anycast IP | Yes | No (per-region) | Yes |
| CDN integrated | Cloud CDN | CloudFront (separate) | Built-in |
| WAF | Cloud Armor | AWS WAF | Azure WAF |
| Serverless backend | Yes | Yes | Yes |
