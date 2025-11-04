# Azure Front Door vs Traffic Manager vs Application Gateway

## Azure Traffic Manager (DNS-based)
- DNS-level routing (returns IP, not proxy)
- Routing methods: Priority, Weighted, Performance, Geographic
- No SSL termination, no caching
- Works with any endpoint (Azure, external, on-prem)
- **Use for**: Multi-region failover, geographic routing

## Azure Application Gateway (L7 Regional)
- Regional L7 load balancer
- SSL termination, WAF, URL-based routing
- Autoscaling (v2), zone-redundant
- WebSocket and HTTP/2 support
- **Use for**: Regional web app load balancing with WAF

## Azure Front Door (Global L7)
- Global L7 load balancer + CDN + WAF
- Anycast routing to nearest PoP
- SSL termination at the edge
- URL rewrite, caching, compression
- Health probes with automatic failover
- **Use for**: Global apps, CDN + WAF + load balancing in one

## Decision Guide
| Need | Service |
|------|---------|
| DNS failover, no proxy | Traffic Manager |
| Regional LB + WAF | Application Gateway |
| Global LB + CDN + WAF | Front Door |
| Simple CDN only | Azure CDN |

## Comparison with AWS/GCP
| Azure | AWS | GCP |
|-------|-----|-----|
| Front Door | CloudFront + ALB + WAF | Cloud CDN + Cloud Armor |
| App Gateway | ALB + WAF | Regional HTTPS LB |
| Traffic Manager | Route 53 | Cloud DNS routing |
