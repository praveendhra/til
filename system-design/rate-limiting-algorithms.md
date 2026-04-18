# Rate Limiting Algorithms — Deep Dive

## Why Rate Limiting?

- Prevent abuse (brute force, scraping, DDoS)
- Protect resources (DB connections, API quotas)
- Fair usage across tenants (multi-tenant SaaS)
- Cost control (cloud API calls)

## Algorithms Compared

### 1. Token Bucket

The most common algorithm. Used by **AWS API Gateway**, **Stripe**, **NGINX**.

```
┌─────────────────────────┐
│     Token Bucket        │
│                         │
│  Capacity: 10 tokens    │
│  Refill: 2 tokens/sec   │
│  Current: ████████░░    │
│           (8 tokens)    │
└─────────────────────────┘

Request arrives → Take 1 token → Allow
No tokens left → Reject (429)
```

**Pros**: Allows bursts up to bucket capacity, smooth long-term rate
**Cons**: Memory per user (bucket state)

```python
import time

class TokenBucket:
    def __init__(self, capacity: int, refill_rate: float):
        self.capacity = capacity
        self.tokens = capacity
        self.refill_rate = refill_rate  # tokens per second
        self.last_refill = time.monotonic()

    def allow(self) -> bool:
        now = time.monotonic()
        elapsed = now - self.last_refill
        self.tokens = min(self.capacity, self.tokens + elapsed * self.refill_rate)
        self.last_refill = now

        if self.tokens >= 1:
            self.tokens -= 1
            return True
        return False
```

### 2. Leaky Bucket

Requests enter a queue (bucket) and are processed at a **fixed rate**. Overflow is rejected.

```
Incoming         Queue (bucket)        Outgoing
requests  ──►  [████████████]  ──►  fixed rate
                    ▲                 (2 req/sec)
                overflow → 429
```

**Pros**: Perfectly smooth output rate, good for APIs needing constant throughput
**Cons**: Recent requests may wait behind old ones; no burst allowance

**Used by**: Shopify, NGINX (`limit_req` with `burst`)

### 3. Fixed Window Counter

Divide time into fixed windows (e.g., 60-second intervals). Count requests per window.

```
Window 1 (00:00-01:00): count = 95 / limit 100 ✓
Window 2 (01:00-02:00): count = 42 / limit 100 ✓
```

**Problem — Boundary burst**: A user sends 100 requests at 0:59 and 100 at 1:01 → 200 requests in 2 seconds, but both windows show under limit.

```
Window 1:     ........................████ (100 at end)
Window 2:     ████........................ (100 at start)
              ↑                          ↑
              0:00                     2:00
              
              200 requests in ~2 seconds!
```

**Pros**: Simple, low memory (one counter per window)
**Cons**: Boundary burst problem

### 4. Sliding Window Log

Keep a **sorted set of timestamps** for each request. On new request, remove expired entries, count remaining.

```python
import time
from collections import deque

class SlidingWindowLog:
    def __init__(self, limit: int, window_seconds: int):
        self.limit = limit
        self.window = window_seconds
        self.requests = deque()  # timestamps

    def allow(self) -> bool:
        now = time.monotonic()
        # Remove expired entries
        while self.requests and self.requests[0] <= now - self.window:
            self.requests.popleft()

        if len(self.requests) < self.limit:
            self.requests.append(now)
            return True
        return False
```

**Pros**: Perfectly accurate, no boundary issues
**Cons**: High memory (stores every timestamp). For 1M users × 100 req/min = 100M timestamps.

### 5. Sliding Window Counter (Best of Both)

Combines fixed window counter with interpolation. **This is what most production systems use.**

```
Previous window count: 84
Current window count: 36
Current position in window: 25% through

Weighted count = 84 × 0.75 + 36 = 99

If limit is 100 → Allow (99 < 100)
```

**Pros**: Smooth, low memory (two counters per window), no boundary burst
**Cons**: Approximate (but very close in practice)

**Used by**: Cloudflare, Kong

## Comparison Matrix

| Algorithm | Memory | Accuracy | Burst Handling | Complexity |
|-----------|--------|----------|---------------|------------|
| Token Bucket | Low | Good | ✅ Allows bursts | Low |
| Leaky Bucket | Low | Perfect | ❌ No bursts | Low |
| Fixed Window | Very Low | Poor (boundary) | ⚠️ Boundary issue | Very Low |
| Sliding Log | High | Perfect | ✅ Precise | Medium |
| Sliding Window Counter | Low | Very Good | ✅ Smooth | Low |

## Distributed Rate Limiting

### Challenge
Rate limiting across multiple servers sharing state.

### Approach 1: Centralized (Redis)
```
All API servers → Redis (INCR + EXPIRE) → shared counter
```

```python
# Redis sliding window counter (pseudocode)
def is_rate_limited(user_id: str, limit: int, window: int) -> bool:
    key = f"rate:{user_id}:{int(time.time()) // window}"
    count = redis.incr(key)
    if count == 1:
        redis.expire(key, window)
    return count > limit
```

**Pros**: Accurate, consistent
**Cons**: Redis becomes SPOF, adds latency (~1ms per check)

### Approach 2: Local + Sync
Each server has local counters, periodically syncs to central store.
- Trade-off: May slightly over-allow during sync gaps
- Used when Redis latency is unacceptable

### Approach 3: Redis Cluster with Lua Scripts
Atomic operations via Lua scripts avoid race conditions:
```lua
-- Redis Lua script for token bucket
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local data = redis.call('HMGET', key, 'tokens', 'last_refill')
local tokens = tonumber(data[1]) or capacity
local last_refill = tonumber(data[2]) or now

local elapsed = now - last_refill
tokens = math.min(capacity, tokens + elapsed * refill_rate)

if tokens >= 1 then
    tokens = tokens - 1
    redis.call('HMSET', key, 'tokens', tokens, 'last_refill', now)
    redis.call('EXPIRE', key, capacity / refill_rate * 2)
    return 1  -- allowed
else
    redis.call('HMSET', key, 'tokens', tokens, 'last_refill', now)
    return 0  -- denied
end
```

## HTTP Response Headers (Standard)

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 30
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1625097600
```

## Rate Limiting Strategies by Layer

| Layer | Tool | Granularity |
|-------|------|-------------|
| CDN/Edge | Cloudflare, AWS WAF | IP, path, country |
| API Gateway | Kong, NGINX, AWS API GW | API key, user, route |
| Application | Custom middleware | User, tenant, resource |
| Database | Connection pooling | Connections per service |

## Interview Answer

> "I'd implement rate limiting using a sliding window counter backed by Redis for distributed scenarios. Each API server checks Redis with an atomic Lua script — this avoids race conditions and keeps the check to ~1ms. For different tiers (free/paid), I'd configure different limits per API key. The sliding window counter gives us accuracy without the memory overhead of logging every request timestamp. I'd return standard rate limit headers (X-RateLimit-Remaining, Retry-After) so clients can back off gracefully."

## Common Questions

**Q: How do you handle rate limiting in a microservices architecture?**
A: Use an API gateway (Kong, NGINX) for global rate limiting at the edge. Individual services can have their own limits for service-to-service calls. Use a shared Redis for consistent counts.

**Q: What about rate limiting WebSocket connections?**
A: Rate limit both connection establishment and message frequency. Token bucket works well for message-level limiting.

**Q: How does Cloudflare handle billions of requests?**
A: Edge-level rate limiting with per-PoP counters, sliding window counters, and IP reputation scoring. They don't need perfect global accuracy — per-PoP is good enough.
