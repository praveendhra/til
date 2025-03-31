# Rate Limiting Algorithms

## 1. Token Bucket
- Tokens added at fixed rate, bucket has max capacity
- Request consumes a token; rejected if bucket empty
- Allows bursts up to bucket size
```
bucket_size = 10, refill_rate = 1/sec
→ Can handle burst of 10, then 1 req/sec sustained
```

## 2. Leaky Bucket
- Requests enter a FIFO queue (the bucket)
- Processed at a constant rate
- Smooths out bursts into steady flow

## 3. Fixed Window
- Count requests in fixed time windows (e.g., per minute)
- Reset counter at window boundary
- Problem: burst at window boundaries (2x rate)

## 4. Sliding Window Log
- Store timestamp of each request
- Count requests in rolling window
- Accurate but memory-intensive

## 5. Sliding Window Counter
- Combine fixed window counts with weighted overlap
- `count = current_window_count + previous_window_count × overlap_percentage`
- Good balance of accuracy and efficiency

## Comparison
| Algorithm | Memory | Accuracy | Burst Handling |
|-----------|--------|----------|---------------|
| Token Bucket | Low | Good | Allows controlled bursts |
| Leaky Bucket | Low | Good | Smooths all traffic |
| Fixed Window | Low | Fair | Edge-case bursts |
| Sliding Log | High | Exact | No bursts |
| Sliding Counter | Low | Good | Minimal edge cases |

## Redis Implementation (Sliding Window)
```python
def is_rate_limited(user_id, limit=100, window=60):
    key = f"rate:{user_id}"
    now = time.time()
    pipe = redis.pipeline()
    pipe.zremrangebyscore(key, 0, now - window)
    pipe.zadd(key, {str(now): now})
    pipe.zcard(key)
    pipe.expire(key, window)
    _, _, count, _ = pipe.execute()
    return count > limit
```
