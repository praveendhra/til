# Rate Limiting Algorithms

## 1. Token Bucket
- Bucket holds N tokens, refills at rate R
- Each request takes a token. Empty bucket = rejected
- **Allows bursts** up to bucket size
- Used by: AWS API Gateway, Stripe

## 2. Leaky Bucket
- Requests enter a queue (bucket), processed at fixed rate
- Overflow = rejected
- **Smooths out bursts** to constant rate

## 3. Fixed Window Counter
- Count requests per time window (e.g., per minute)
- Reset counter at window boundary
- **Problem**: Burst at window edges (2x rate in 2-second span)

## 4. Sliding Window Log
- Store timestamp of each request
- Count requests in last N seconds
- **Accurate** but memory-intensive

## 5. Sliding Window Counter
- Weighted combination of current + previous window
- `rate = prev_count * overlap% + cur_count`
- Good balance of accuracy and memory

## Implementation in Distributed Systems
Use **Redis** with atomic operations:
```
MULTI
INCR rate:{user_id}:{window}
EXPIRE rate:{user_id}:{window} 60
EXEC
```

## Cloud API Gateways
- **AWS API Gateway**: Token bucket, configurable per stage/method
- **Azure API Management**: Policy-based rate limiting
- **GCP Apigee**: Spike arrest + quota policies
