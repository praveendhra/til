# Load Balancer Algorithms — Complete Guide

## Why Load Balancing?

Distribute traffic across multiple servers to:
- **Increase throughput**: Handle more requests
- **Improve availability**: If one server dies, others handle traffic
- **Reduce latency**: Route to nearest/fastest server

## Algorithms

### 1. Round Robin

Requests cycle through servers: A → B → C → A → B → C...

```
Request 1 → Server A
Request 2 → Server B
Request 3 → Server C
Request 4 → Server A
...
```

**Pros**: Simple, even distribution with identical servers
**Cons**: Ignores server capacity and current load
**Used by**: DNS round-robin, NGINX default, ELB basic

### 2. Weighted Round Robin

Servers get traffic proportional to weight.

```
Server A (weight 5): handles 5 out of every 8 requests
Server B (weight 2): handles 2 out of every 8
Server C (weight 1): handles 1 out of every 8
```

**Use case**: Heterogeneous servers (16-core vs 4-core machines), gradual canary rollouts (new version gets weight 1, old gets weight 9)

### 3. Least Connections

Route to the server with the **fewest active connections**.

```
Server A: 12 active connections
Server B: 5 active connections   ← next request goes here
Server C: 8 active connections
```

**Pros**: Adapts to slow requests (long connections stay counted)
**Cons**: Doesn't account for server capacity
**Best for**: Long-lived connections (WebSocket, gRPC streaming, database proxies)
**Used by**: HAProxy, AWS ALB

### 4. Weighted Least Connections

Combines weights with connection count: `score = active_connections / weight`

```
Server A: 12 conns, weight 4 → score 3.0
Server B: 5 conns, weight 2  → score 2.5  ← lowest, gets next request
Server C: 8 conns, weight 1  → score 8.0
```

### 5. IP Hash

Hash client IP to deterministically route to a server. Same client always hits same server.

```
hash("192.168.1.100") % 3 = 1 → always Server B
```

**Pros**: Session affinity without cookies, good for caching
**Cons**: Uneven distribution, adding/removing servers changes mappings
**Use case**: Stateful applications, sticky sessions

### 6. Least Response Time

Route to the server with the **lowest average response time** AND fewest connections.

**Pros**: Best real-time performance optimization
**Cons**: Requires active health monitoring, oscillation possible
**Used by**: NGINX Plus, F5 BIG-IP

### 7. Random

Pick a server randomly.

**Pros**: Simple, no state needed, works well at scale
**Cons**: Not optimal for small server counts
**Fun fact**: "Power of Two Random Choices" — pick 2 random servers, route to the one with fewer connections. This is surprisingly effective and used in Envoy proxy.

### 8. Consistent Hashing

See dedicated TIL on consistent hashing. Maps requests to a hash ring.

**Best for**: Distributed caches (Memcached, CDN), where routing stability matters

## Comparison

| Algorithm | Stateful? | Adaptive? | Best For |
|-----------|----------|-----------|----------|
| Round Robin | No | No | Homogeneous, stateless services |
| Weighted RR | No | No | Mixed-capacity servers |
| Least Connections | Yes | Yes | Long-lived connections, variable latency |
| IP Hash | No | No | Sticky sessions, caching |
| Least Response Time | Yes | Yes | Performance-critical, heterogeneous |
| Random (P2C) | Minimal | Yes | Large-scale, low-overhead |

## Layer 4 vs Layer 7 Load Balancing

| Feature | Layer 4 (TCP/UDP) | Layer 7 (HTTP) |
|---------|-------------------|----------------|
| Routing based on | IP, port | URL, headers, cookies, body |
| Speed | Very fast | Slower (must parse HTTP) |
| SSL termination | Pass-through or terminate | Always terminates |
| Content routing | ❌ | ✅ `/api/*` → backend, `/static/*` → CDN |
| WebSocket | Pass-through | Can inspect upgrade |
| Examples | AWS NLB, HAProxy (TCP mode) | AWS ALB, NGINX, Envoy |

```
Layer 4:
Client ──TCP──► LB ──TCP──► Server
(LB just forwards packets, very fast)

Layer 7:
Client ──HTTPS──► LB (terminates TLS, reads HTTP) ──HTTP──► Server
(LB can route by path, add headers, rewrite URLs)
```

## Health Checks

Load balancers must detect failed servers:

```yaml
# Kubernetes readiness probe
readinessProbe:
  httpGet:
    path: /healthz
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 10
  failureThreshold: 3

# NGINX upstream health check
upstream backend {
    server 10.0.0.1:8080 max_fails=3 fail_timeout=30s;
    server 10.0.0.2:8080 max_fails=3 fail_timeout=30s;
}
```

Types of health checks:
- **Active**: LB periodically probes servers (HTTP GET /healthz)
- **Passive**: LB monitors response codes from real traffic
- **Deep**: Check dependencies (DB connection, disk space)

## Global Load Balancing (GSLB)

For multi-region deployments:

```
User (Tokyo) → DNS → GSLB → Route to Asia-Pacific region

GSLB considers:
  - Geographic proximity (latency-based routing)
  - Server health across regions
  - Regional capacity
```

**Tools**: AWS Route 53 (latency/geo routing), Cloudflare, Azure Traffic Manager, GCP Cloud DNS

## Real-World Architecture

```
Internet
    │
  ┌─┴──┐
  │ CDN │ (static assets, edge caching)
  └─┬──┘
    │
  ┌─┴──────┐
  │ Global │ (DNS-based, Route 53)
  │   LB   │
  └─┬──────┘
    │
  ┌─┴──────┐
  │Regional│ (L7, ALB/NGINX)
  │   LB   │ (TLS termination, path routing)
  └─┬──┬──┘
    │  │
  ┌─┘  └─┐
  │      │
App    App  (auto-scaled instances)
```

## Interview Answer

> "I'd use an L7 load balancer like AWS ALB or NGINX for HTTP traffic — it gives us content-based routing, TLS termination, and health checks. For the algorithm, least connections works best for services with variable response times. For global distribution, I'd use DNS-based load balancing (Route 53 latency routing) to direct users to the nearest region, with regional ALBs handling the actual request distribution. Health checks should be both active (periodic probes) and deep (verify downstream dependencies are healthy)."
