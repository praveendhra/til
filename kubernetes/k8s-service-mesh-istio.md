# Kubernetes Service Mesh (Istio)

## What a Service Mesh Solves

Moves cross-cutting concerns out of application code into infrastructure:
- mTLS between services (automatic encryption)
- Traffic management (canary, retries, timeouts)
- Observability (distributed tracing, metrics)
- Access control (which service can talk to which)

## Architecture

```
┌─────────────────────────────────────────┐
│  Control Plane (istiod)                 │
│  - Pilot (config)                       │
│  - Citadel (certs)                      │
│  - Galley (validation)                  │
└──────────────┬──────────────────────────┘
               │ pushes config
    ┌──────────▼──────────┐
    │  Data Plane          │
    │  ┌─────┐  ┌─────┐   │
    │  │App A│  │App B│   │
    │  │Envoy│──│Envoy│   │
    │  └─────┘  └─────┘   │
    └──────────────────────┘
```

Every pod gets an Envoy sidecar proxy injected automatically.

## Traffic Management

```yaml
# Canary: send 10% traffic to v2
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: reviews
spec:
  hosts:
    - reviews
  http:
    - route:
        - destination:
            host: reviews
            subset: v1
          weight: 90
        - destination:
            host: reviews
            subset: v2
          weight: 10
---
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
    - name: v1
      labels:
        version: v1
    - name: v2
      labels:
        version: v2
```

## Resilience

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: ratings
spec:
  hosts:
    - ratings
  http:
    - timeout: 3s
      retries:
        attempts: 3
        perTryTimeout: 1s
        retryOn: 5xx,reset,connect-failure
      route:
        - destination:
            host: ratings
```

## Authorization Policy

```yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: allow-orders-to-inventory
  namespace: default
spec:
  selector:
    matchLabels:
      app: inventory
  action: ALLOW
  rules:
    - from:
        - source:
            principals: ["cluster.local/ns/default/sa/orders"]
      to:
        - operation:
            methods: ["GET", "POST"]
            paths: ["/api/stock/*"]
```

## Observability (built-in)

- **Metrics**: Request rate, error rate, latency (RED) per service — scrape with Prometheus
- **Traces**: Distributed traces via Jaeger/Zipkin (headers propagated automatically)
- **Access logs**: Envoy access logs for every request
- **Kiali**: Service mesh topology visualization

## Alternatives to Istio

| Mesh | Key Difference |
|------|---------------|
| Linkerd | Lighter weight, Rust proxy, simpler ops |
| Cilium | eBPF-based (no sidecar), L3/L4 + L7 |
| Consul Connect | HashiCorp ecosystem, multi-platform |

## When NOT to Use a Service Mesh

- Small number of services (< 5-10)
- Team doesn't have capacity to operate it
- Latency-critical paths where sidecar overhead matters
- Simple retry/timeout needs solvable with client libraries
