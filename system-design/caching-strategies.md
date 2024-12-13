# Caching Strategies

## Read Strategies

### Cache-Aside (Lazy Loading)
App checks cache first → miss → read DB → populate cache.
```
if cache.get(key) is None:
    data = db.query(key)
    cache.set(key, data, ttl=300)
return cache.get(key)
```
Most common pattern. Used by default in most apps.

### Read-Through
Cache itself handles the DB read on miss. App only talks to cache.

## Write Strategies

### Write-Through
Write to cache AND DB synchronously. Strong consistency, higher write latency.

### Write-Behind (Write-Back)
Write to cache immediately, async write to DB. Fast writes, risk of data loss.

### Write-Around
Write directly to DB, skip cache. Cache gets populated on next read miss.

## Eviction Policies
- **LRU** (Least Recently Used) — most common
- **LFU** (Least Frequently Used) — good for skewed access patterns
- **TTL** (Time To Live) — expiration-based

## Cloud Services
- **AWS**: ElastiCache (Redis/Memcached), DAX (DynamoDB cache)
- **Azure**: Azure Cache for Redis
- **GCP**: Memorystore (Redis/Memcached)
