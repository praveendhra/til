# Back-of-the-Envelope Estimation

## Why This Matters in Interviews

System design interviews often start with: "How much storage do we need?" or "How many servers?" You need to estimate quickly using powers of 2 and rough numbers.

## Essential Numbers to Memorize

### Data Size
```
1 Byte     = 8 bits
1 KB       = 1,000 bytes (10³)
1 MB       = 1,000,000 bytes (10⁶)
1 GB       = 10⁹ bytes
1 TB       = 10¹² bytes
1 PB       = 10¹⁵ bytes

Character (ASCII)  = 1 byte
Character (UTF-8)  = 1-4 bytes
Integer (int32)    = 4 bytes
Long (int64)       = 8 bytes
UUID               = 16 bytes
Timestamp          = 8 bytes

Average tweet      ≈ 140 bytes (text only)
Average URL        ≈ 100 bytes
Average email      ≈ 50 KB
Average photo      ≈ 200 KB (compressed)
Average web page   ≈ 2 MB
1 min HD video     ≈ 50 MB
1 min 4K video     ≈ 300 MB
```

### Latency Numbers (Jeff Dean, roughly still valid)
```
L1 cache reference                     0.5 ns
L2 cache reference                       7 ns
Main memory reference                  100 ns
SSD random read                    150,000 ns  (150 μs)
HDD random read                10,000,000 ns  (10 ms)
Send 1 KB over network            250,000 ns  (250 μs)
Read 1 MB from SSD               1,000,000 ns  (1 ms)
Read 1 MB from HDD              20,000,000 ns  (20 ms)
Round trip within datacenter       500,000 ns  (0.5 ms)
Round trip US East → West       40,000,000 ns  (40 ms)
Round trip US → Europe          80,000,000 ns  (80 ms)

Key insight:
  Memory is 1000x faster than SSD
  SSD is 100x faster than HDD
  Datacenter RTT ≈ 0.5ms
  Cross-continent RTT ≈ 40-80ms
```

### Throughput Numbers
```
QPS that a single server can handle:
  Web server (Node.js, Flask)    ≈ 1,000-10,000 QPS
  Application server (Go, Java) ≈ 10,000-100,000 QPS
  Redis                         ≈ 100,000-500,000 QPS
  MySQL (simple queries)        ≈ 5,000-10,000 QPS
  PostgreSQL (simple queries)   ≈ 5,000-20,000 QPS
  Kafka (single broker)         ≈ 100,000 messages/sec

Network bandwidth:
  1 Gbps  = 125 MB/s
  10 Gbps = 1.25 GB/s
```

### Scale Numbers
```
Daily Active Users (DAU) of major services (2024 approx):
  Facebook    ≈ 2 billion
  YouTube     ≈ 800 million
  Instagram   ≈ 500 million
  Twitter/X   ≈ 250 million
  Netflix     ≈ 100 million
  Slack       ≈ 30 million

Seconds in a day:  86,400  ≈ 10⁵ (use 100,000)
Seconds in a year: 31,536,000 ≈ 3 × 10⁷
```

## Estimation Framework

### Step 1: Clarify Requirements
- DAU? (Daily Active Users)
- Read:write ratio?
- Data per item?
- Retention period?
- Peak vs average?

### Step 2: Traffic Estimation
```
Example: URL shortener with 100M DAU

Reads (redirects): 100M DAU × 5 reads/day = 500M reads/day
Writes (new URLs): Read:write = 100:1 → 5M writes/day

QPS (average): 500M / 86,400 ≈ 5,800 QPS
QPS (peak): 5,800 × 3 ≈ 17,400 QPS (assume 3x peak)

Writes QPS: 5M / 86,400 ≈ 58 QPS (very manageable)
```

### Step 3: Storage Estimation
```
Each shortened URL record:
  - Short URL (7 chars)  = 7 bytes
  - Long URL (avg)       = 100 bytes
  - Created timestamp    = 8 bytes
  - User ID              = 8 bytes
  - Total per record     ≈ 200 bytes (with overhead)

5M writes/day × 200 bytes = 1 GB/day
1 GB/day × 365 days × 5 years = 1.8 TB total

→ Single database can handle this easily
```

### Step 4: Bandwidth Estimation
```
Incoming (writes):
  5M × 200 bytes / 86,400 ≈ 12 KB/s (negligible)

Outgoing (reads, returning redirect):
  500M × 200 bytes / 86,400 ≈ 1.2 MB/s (very manageable)
```

### Step 5: Server Estimation
```
If each server handles 10,000 QPS:
  Peak QPS 17,400 / 10,000 ≈ 2 servers (plus redundancy)
  → 4-6 servers with some buffer
```

## Practice Examples

### Twitter-like Feed System
```
Given: 300M DAU, avg 2 tweets/day, avg tweet 200 bytes, 500 follows avg

Writes: 300M × 2 = 600M tweets/day = 7,000 tweets/sec
Storage: 600M × 200 bytes = 120 GB/day ≈ 44 TB/year

Read (timeline): 300M × 10 views/day = 3B reads/day = 35,000 QPS
Fan-out: 600M tweets × 500 followers = 300B deliveries/day
  → This is why Twitter uses a hybrid push/pull model!
```

### Chat System (WhatsApp-like)
```
Given: 500M DAU, avg 40 messages/day, avg message 100 bytes

Messages: 500M × 40 = 20B messages/day
Storage: 20B × 100 bytes = 2 TB/day ≈ 730 TB/year
QPS: 20B / 86,400 ≈ 230,000 messages/sec

→ Need significant horizontal scaling + message queues
```

## Common Mistakes

1. **Forgetting peak vs average**: Peak is typically 2-5x average
2. **Wrong units**: Mixing bits and bytes
3. **Ignoring metadata**: Indexes, replication, overhead add 2-3x
4. **Not rounding**: Use round numbers! 86,400 → 100,000
5. **Overthinking**: Interviewers want the approach, not exact numbers

## Quick Reference Cheat Sheet

```
Power   Exact           Approx     Bytes
2¹⁰     1,024          1 thousand  1 KB
2²⁰     1,048,576      1 million   1 MB
2³⁰     1,073,741,824  1 billion   1 GB
2⁴⁰     1.1 × 10¹²     1 trillion  1 TB

1 million requests/day  ≈ 12 QPS
1 billion requests/day  ≈ 12,000 QPS
86,400 seconds/day      ≈ 10⁵
2.5 million seconds/month ≈ 2.5 × 10⁶
```
