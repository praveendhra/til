# GCP Load Balancing

## Types
| Type | Scope | Layer | Protocol |
|------|-------|-------|----------|
| External HTTP(S) LB | Global | L7 | HTTP/HTTPS |
| External TCP/UDP LB | Regional | L4 | TCP/UDP |
| Internal HTTP(S) LB | Regional | L7 | HTTP/HTTPS |
| Internal TCP/UDP LB | Regional | L4 | TCP/UDP |

## Global HTTP(S) Load Balancer
```
User → Global Anycast IP → Google Edge POP → Backend (nearest region)
```

Features:
- Single anycast IP for all regions
- Content-based routing (URL maps)
- Cloud CDN integration
- Cloud Armor (WAF) integration
- SSL termination at the edge
- Automatic SSL certificates

## URL Map Example
```yaml
defaultService: backend-service-default
hostRules:
  - hosts: ["api.example.com"]
    pathMatcher: api-paths
pathMatchers:
  - name: api-paths
    defaultService: api-backend
    pathRules:
      - paths: ["/v1/*"]
        service: api-v1-backend
      - paths: ["/v2/*"]
        service: api-v2-backend
```

## Health Checks
```bash
gcloud compute health-checks create http my-check \
  --port=8080 \
  --request-path=/health \
  --check-interval=10s \
  --healthy-threshold=2 \
  --unhealthy-threshold=3
```
