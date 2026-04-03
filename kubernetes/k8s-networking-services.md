# Kubernetes Networking & Services — Deep Dive

## The Kubernetes Networking Model

Every Pod gets its own IP address. Four networking problems K8s solves:

1. **Pod-to-Pod** (same node): Virtual bridge (cbr0) — direct communication
2. **Pod-to-Pod** (cross-node): Overlay network (Calico, Flannel, Cilium)
3. **Pod-to-Service**: kube-proxy + iptables/IPVS
4. **External-to-Service**: Ingress controllers, LoadBalancer services

```
┌─────────────── Node 1 ───────────────┐    ┌─────────────── Node 2 ───────────────┐
│  ┌──────┐  ┌──────┐                 │    │                  ┌──────┐  ┌──────┐  │
│  │Pod A │  │Pod B │                 │    │                  │Pod C │  │Pod D │  │
│  │.1.10 │  │.1.11 │                 │    │                  │.2.10 │  │.2.11 │  │
│  └──┬───┘  └──┬───┘                 │    │                  └──┬───┘  └──┬───┘  │
│     └────┬────┘                     │    │                     └────┬────┘      │
│       veth pairs                    │    │                       veth pairs     │
│     ┌────┴────┐                     │    │                     ┌────┴────┐      │
│     │  cbr0   │                     │    │                     │  cbr0   │      │
│     │ bridge  │                     │    │                     │ bridge  │      │
│     └────┬────┘                     │    │                     └────┬────┘      │
│          │                          │    │                          │           │
│     ┌────┴────┐                     │    │                     ┌────┴────┐      │
│     │  eth0   │─────────────────────┼────┼─────────────────────│  eth0   │      │
│     └─────────┘                     │    │                     └─────────┘      │
└─────────────────────────────────────┘    └─────────────────────────────────────┘
              │              Overlay Network (VXLAN, IPinIP, WireGuard)        │
              └──────────────────────────┬─────────────────────────────────────┘
```

## Service Types

### ClusterIP (Default)
Internal-only virtual IP. Only accessible within the cluster.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-api
spec:
  type: ClusterIP  # default
  selector:
    app: my-api
  ports:
    - port: 80        # Service port (what consumers use)
      targetPort: 8080 # Pod port (what the app listens on)
```

DNS: `my-api.default.svc.cluster.local`

### NodePort
Exposes service on each node's IP at a static port (30000-32767).

```yaml
spec:
  type: NodePort
  ports:
    - port: 80
      targetPort: 8080
      nodePort: 31000  # accessible at <any-node-ip>:31000
```

**Use case**: Dev/test, on-prem without cloud LB

### LoadBalancer
Creates a cloud provider load balancer (AWS ELB/NLB, Azure LB, GCP LB).

```yaml
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 8080
  # Cloud provider creates an external LB automatically
```

**Use case**: Production external traffic. Each LoadBalancer service = one cloud LB ($$$).

### ExternalName
DNS CNAME alias to an external service.

```yaml
spec:
  type: ExternalName
  externalName: database.example.com
  # my-db.default.svc.cluster.local → CNAME → database.example.com
```

**Use case**: Referencing external services (managed DB) via Kubernetes DNS.

## Ingress — The Real Production Pattern

Instead of one LoadBalancer per service, use ONE Ingress controller for all HTTP routing.

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx
  tls:
    - hosts: [api.example.com]
      secretName: api-tls
  rules:
    - host: api.example.com
      http:
        paths:
          - path: /v1/users
            pathType: Prefix
            backend:
              service:
                name: user-service
                port:
                  number: 80
          - path: /v1/orders
            pathType: Prefix
            backend:
              service:
                name: order-service
                port:
                  number: 80
```

**Popular Ingress Controllers**: NGINX Ingress, Traefik, HAProxy, AWS ALB Ingress, Istio Gateway

## kube-proxy Modes

| Mode | How It Works | Performance | Scale |
|------|-------------|-------------|-------|
| **iptables** (default) | Chains of iptables rules | O(n) rule matching | Good to ~5K services |
| **IPVS** | Linux kernel load balancer | O(1) lookup | Better for 5K+ services |
| **nftables** | Modern iptables replacement | Similar to iptables | Newer clusters |

## CNI (Container Network Interface) Plugins

| Plugin | Overlay | NetworkPolicy | Performance | Features |
|--------|---------|---------------|-------------|----------|
| **Calico** | IPinIP, VXLAN, none | ✅ Full | Excellent | BGP, eBPF mode |
| **Cilium** | VXLAN, Geneve | ✅ Full + L7 | Excellent | eBPF, observability |
| **Flannel** | VXLAN | ❌ Basic | Good | Simple, minimal |
| **Weave** | VXLAN | ✅ Basic | Good | Encryption |
| **AWS VPC CNI** | None (native VPC) | ✅ | Best (no overlay) | AWS-specific |

**Recommendation**: Calico for most clusters, Cilium for advanced observability, AWS VPC CNI on EKS.

## NetworkPolicy — Micro-segmentation

By default, all pods can talk to all pods. NetworkPolicies restrict this.

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: api-only-from-frontend
  namespace: production
spec:
  podSelector:
    matchLabels:
      app: api
  policyTypes:
    - Ingress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: frontend
        - namespaceSelector:
            matchLabels:
              name: monitoring  # Allow Prometheus scraping
      ports:
        - protocol: TCP
          port: 8080
```

## DNS in Kubernetes

CoreDNS resolves service names:

```
# Service DNS:
<service>.<namespace>.svc.cluster.local

# Pod DNS:
<pod-ip-dashed>.<namespace>.pod.cluster.local

# Headless service (clusterIP: None):
# Returns Pod IPs directly (used by StatefulSets)
<pod-name>.<service>.<namespace>.svc.cluster.local
```

## Interview Answer

> "Kubernetes networking is based on the principle that every Pod gets a unique IP and can communicate with any other Pod without NAT. Services provide stable endpoints — ClusterIP for internal, LoadBalancer for external. In production, I use an NGINX Ingress controller with cert-manager for TLS, which consolidates all HTTP routing into one load balancer. For network security, I implement NetworkPolicies to enforce zero-trust between namespaces. On EKS, I prefer the VPC CNI for native performance; on other platforms, Calico or Cilium for their NetworkPolicy support and eBPF-based observability."
