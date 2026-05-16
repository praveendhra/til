# System Design: URL Shortener (like bit.ly)

## Requirements

### Functional
- Given a long URL, generate a short URL
- Given a short URL, redirect to the original URL
- Optional: Custom short URLs, expiration, analytics

### Non-Functional
- Very low latency for redirects (< 50ms)
- High availability (redirects must always work)
- Short URLs should not be guessable (no sequential IDs)

## Capacity Estimation

```
Assumptions:
  100M new URLs per month
  Read:write ratio = 100:1 (reads dominate)
  Retention: 5 years

Writes: 100M / (30 × 86400) ≈ 40 URLs/sec
Reads: 40 × 100 = 4,000 redirects/sec
Peak: 4,000 × 3 = 12,000 redirects/sec

Storage (5 years):
  100M × 12 months × 5 years = 6B URLs
  Each record ≈ 500 bytes (short URL + long URL + metadata)
  Total: 6B × 500 = 3 TB

Cache:
  80/20 rule: 20% of URLs get 80% of traffic
  Daily reads: 4,000 × 86400 = 345M
  Cache 20%: 69M × 500 bytes ≈ 35 GB (fits in Redis)
```

## Short URL Generation

### Approach 1: Hash + Truncate
```
MD5("https://example.com/very/long/url") → "a3f5b2c8d1e9..."
Take first 7 characters → "a3f5b2c"
Short URL: https://short.url/a3f5b2c

Collision? Append random chars and retry
```
Characters: [a-zA-Z0-9] = 62 characters
7 chars = 62⁷ = 3.5 trillion unique URLs (plenty)

### Approach 2: Counter-Based (Base62 Encoding)
```
Auto-increment counter: 1, 2, 3, ... 10000000001
Base62 encode: 10000000001 → "aUc1x3"

Counter service: Single point (ZooKeeper-allocated ranges)
  Server 1: range [1 - 1000000]
  Server 2: range [1000001 - 2000000]
  Each server uses its range independently (no coordination)
```

### Approach 3: Pre-Generated Keys (KGS)
```
Key Generation Service pre-generates millions of keys
Stores in DB: unused keys and used keys
When needed: grab an unused key, mark as used

Pros: No collision, no computation
Cons: Need to manage the key pool
```

## Architecture

```
Client                                    
  │                                       
  ▼                                       
┌─────────────┐                           
│ API Gateway │ (rate limiting, auth)     
│ / LB        │                           
└──────┬──────┘                           
       │                                  
  ┌────┴────┐                             
  │         │                             
  ▼         ▼                             
┌─────┐  ┌─────┐                          
│Write│  │Read │                          
│ API │  │ API │                          
└──┬──┘  └──┬──┘                          
   │        │                             
   │     ┌──▼──┐                          
   │     │Cache│ (Redis - 35GB)           
   │     │     │ Key: short_url           
   │     └──┬──┘ Value: long_url          
   │        │ (cache miss)                
   │     ┌──▼───────┐                     
   └────►│ Database │                     
         │ (NoSQL)  │                     
         └──────────┘                     
```

### Database Schema
```
Table: urls
  short_url    VARCHAR(7)  PRIMARY KEY  -- "a3f5b2c"
  long_url     TEXT                      -- "https://..."
  user_id      VARCHAR(36)              -- creator
  created_at   TIMESTAMP
  expires_at   TIMESTAMP                -- optional
  click_count  BIGINT DEFAULT 0
```

**Database choice**: DynamoDB or Cassandra (key-value access pattern, high read throughput). Not relational — we never need JOINs.

### Write Flow
```
1. Client: POST /api/shorten {"url": "https://long-url.com/..."}
2. Validate URL
3. Generate short code (hash or counter)
4. Check for collision in DB
5. Store mapping in DB
6. Return short URL
```

### Read Flow (Redirect)
```
1. Client: GET /a3f5b2c
2. Check Redis cache → Hit? Return 301 redirect
3. Cache miss → Query DB
4. Store in Redis cache (TTL: 24h)
5. Return 301 redirect (permanent) or 302 (temporary)
```

**301 vs 302**:
- 301 (Permanent): Browser caches redirect, fewer server hits, can't track clicks
- 302 (Temporary): Every click hits server, enables analytics

### Caching Strategy
```
Cache aside with Redis:
  Key: "a3f5b2c"
  Value: "https://original-long-url.com/path..."
  TTL: 24 hours (for popular URLs)

LRU eviction when cache is full
Bloom filter to check if URL exists (avoid DB for non-existent URLs)
```

## Scaling Considerations

- **Read-heavy**: Cache + read replicas handle 99% of reads
- **Analytics**: Async — write click events to Kafka → process in batch
- **Availability**: Multi-region with global load balancing
- **Rate limiting**: Per-user limits on URL creation (prevent abuse)

## Interview Talking Points

> "A URL shortener is a read-heavy system (100:1 ratio), so caching is critical. I'd use Redis to cache the top 20% of URLs, which handles 80% of traffic. For short code generation, I'd use a counter-based approach with pre-allocated ranges to avoid coordination between servers. The database is DynamoDB or Cassandra since it's purely key-value lookups. For analytics, I'd use 302 redirects and stream click events to Kafka for async processing, keeping the redirect path fast."
