# Caching Strategies — Complete Guide

## Cache-Aside (Lazy Loading) — Most Common

Application manages the cache explicitly.

```
Read:
  1. Check cache → hit? return cached
  2. Miss → read from DB → store in cache → return

Write:
  1. Write to DB
  2. Invalidate/delete cache entry
```

```python
def get_user(user_id):
    # Check cache
    cached = redis.get(f"user:{user_id}")
    if cached:
        return json.loads(cached)

    # Cache miss — read from DB
    user = db.query("SELECT * FROM users WHERE id = %s", user_id)

    # Populate cache
    redis.setex(f"user:{user_id}", 3600, json.dumps(user))
    return user
```

**Pros**: Only requested data is cached, cache failure doesn't break reads
**Cons**: Cache miss = 3 round trips (cache check + DB read + cache write), stale data possible
**Used by**: Most web applications, microservices

### Critical: Delete, Don't Update Cache on Writes

```python
# ✅ CORRECT: Delete cache on write
def update_user(user_id, data):
    db.update("UPDATE users SET ... WHERE id = %s", user_id, data)
    redis.delete(f"user:{user_id}")  # Next read will repopulate

# ❌ WRONG: Update cache on write (race condition!)
def update_user(user_id, data):
    db.update("UPDATE users SET ... WHERE id = %s", user_id, data)
    redis.set(f"user:{user_id}", json.dumps(data))  # Could write stale data!
```

Why? With concurrent writes, the cache might end up with the older write's data:
```
Thread A: DB write (v2) ──────────────────── Cache write (v2)
Thread B: ────── DB write (v3) ── Cache write (v3) ──────────
Result: Cache has v2, DB has v3 → STALE!
```

## Write-Through

Write to cache AND database **synchronously**. Cache is always up-to-date.

```
Write:
  1. Write to cache
  2. Cache writes to DB (synchronously)
  → Both always consistent
```

**Pros**: Cache always consistent with DB, reads never miss
**Cons**: Write latency (two writes), cache may hold data never read
**Used by**: DynamoDB DAX, CPU L1/L2 caches

## Write-Behind (Write-Back)

Write to cache immediately, **asynchronously** flush to DB.

```
Write:
  1. Write to cache → return immediately (fast!)
  2. Background: batch flush dirty entries to DB

Timeline:
  App → Cache (1ms) → [background] → DB (batched, async)
```

**Pros**: Very fast writes, batching reduces DB load
**Cons**: Data loss risk if cache crashes before flush, complex consistency
**Used by**: CPU caches, some ORM frameworks, application write buffers

## Read-Through

Cache sits in front of DB. Cache is responsible for loading data on miss (not the application).

```
Read:
  App → Cache (if miss, cache loads from DB) → return
```

**Pros**: Application code is simpler (just reads from cache)
**Cons**: First request always slow, cache library must know about data source
**Used by**: Guava LoadingCache, Caffeine, CDN origin pulls

## Comparison

| Strategy | Read Perf | Write Perf | Consistency | Data Loss Risk | Complexity |
|----------|-----------|------------|-------------|---------------|------------|
| Cache-Aside | Good (after warm) | Good | Eventual | None | Low |
| Write-Through | Excellent | Slow | Strong | None | Medium |
| Write-Behind | Excellent | Excellent | Eventual | Yes | High |
| Read-Through | Good (after warm) | N/A | Eventual | None | Low |

## Cache Eviction Policies

| Policy | Description | Best For |
|--------|------------|----------|
| **LRU** (Least Recently Used) | Evict least recently accessed | General purpose (80% of cases) |
| **LFU** (Least Frequently Used) | Evict least frequently accessed | Distinguishing hot vs warm data |
| **FIFO** | Evict oldest entry | Simple, predictable |
| **TTL** | Expire after time duration | Sessions, tokens, API responses |
| **Random** | Evict random entry | When LRU overhead is too high |
| **W-TinyLFU** | Windowed TinyLFU (Caffeine) | Best hit rate in benchmarks |

## Cache Invalidation Patterns

### 1. TTL-Based (Time-to-Live)
```python
redis.setex("product:123", 300, data)  # Expires in 5 minutes
```
Simple, but you serve stale data until TTL expires.

### 2. Event-Based Invalidation
```python
# On product update → publish event
def update_product(product_id, data):
    db.update(product_id, data)
    event_bus.publish("product.updated", {"id": product_id})

# Cache invalidation listener
@event_bus.subscribe("product.updated")
def invalidate_cache(event):
    redis.delete(f"product:{event['id']}")
```

### 3. Version-Based
```python
# Cache key includes version
version = db.query("SELECT version FROM products WHERE id = %s", pid)
cache_key = f"product:{pid}:v{version}"
```

## Cache Stampede (Thundering Herd)

When a popular cache key expires, hundreds of concurrent requests all miss cache simultaneously and hit the DB.

```
Cache key expires at T=100
T=100.001: Request 1 → cache miss → DB query
T=100.002: Request 2 → cache miss → DB query
T=100.003: Request 3 → cache miss → DB query
...
T=100.100: Request 100 → cache miss → DB query
→ DB overwhelmed!
```

### Solutions:

**1. Locking (Mutex)**
```python
def get_with_lock(key):
    value = redis.get(key)
    if value:
        return value

    # Only one request rebuilds cache
    if redis.set(f"lock:{key}", "1", nx=True, ex=10):
        value = db.query(...)
        redis.setex(key, 3600, value)
        redis.delete(f"lock:{key}")
        return value
    else:
        time.sleep(0.05)  # Wait for rebuilder
        return redis.get(key)
```

**2. Early Expiration (Probabilistic)**
Refresh cache before it actually expires, with some randomness.

**3. Background Refresh**
A background job refreshes popular keys before they expire.

## Multi-Level Caching

```
Request → L1 (in-process, Caffeine) → L2 (Redis) → Database
           ~1μs                       ~1ms           ~10ms
```

| Level | Technology | Size | Latency | Shared? |
|-------|-----------|------|---------|---------|
| L1 | In-process (Guava, dict) | 100MB | < 1μs | No (per instance) |
| L2 | Redis/Memcached | 100GB+ | ~1ms | Yes (shared) |
| L3 | CDN (CloudFront) | Unlimited | ~10ms | Yes (global) |

## Real-World Numbers

| Scenario | Without Cache | With Cache |
|----------|--------------|------------|
| User profile read | 15ms (DB) | 0.5ms (Redis) |
| Product page | 100ms (3 DB queries) | 5ms (1 cache hit) |
| Feed generation | 500ms (fan-out) | 10ms (pre-computed) |
| API response (CDN) | 200ms (origin) | 20ms (edge) |

## Interview Answer

> "For most applications, I use cache-aside with Redis. On reads, check cache first, fall back to DB on miss and populate cache with a TTL. On writes, update DB then delete the cache key — never update the cache directly to avoid race conditions. For cache stampede, I use a distributed lock so only one request rebuilds the cache while others wait briefly. I'd add an in-process L1 cache (like Caffeine in Java or a simple dict in Python) for ultra-hot keys, with a short TTL (30s) to limit staleness."
