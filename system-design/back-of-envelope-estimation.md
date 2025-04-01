# Back-of-Envelope Estimation Cheat Sheet

## Power of 2
| Power | Value | Bytes |
|-------|-------|-------|
| 10 | 1,024 | 1 KB |
| 20 | ~1M | 1 MB |
| 30 | ~1B | 1 GB |
| 40 | ~1T | 1 TB |

## Latency Numbers Every Programmer Should Know
| Operation | Time |
|-----------|------|
| L1 cache | 0.5 ns |
| L2 cache | 7 ns |
| RAM access | 100 ns |
| SSD random read | 150 μs |
| HDD seek | 10 ms |
| Same datacenter round trip | 0.5 ms |
| Cross-region round trip | 50-150 ms |

## Quick Estimates
- 1 day = 86,400 seconds ≈ 10^5 seconds
- 1 million requests/day ≈ 12 requests/second
- 1 billion requests/day ≈ 12,000 requests/second

## Storage Estimates
- 1 tweet (140 chars) ≈ 300 bytes (with metadata)
- 1 photo (compressed) ≈ 200 KB
- 1 minute of video (720p) ≈ 50 MB
- 1 million users × 1 KB profile = 1 GB

## Database
- Single MySQL node: ~1000-5000 QPS (depends on query)
- Redis: 100,000+ QPS
- DynamoDB: virtually unlimited (auto-scales)
