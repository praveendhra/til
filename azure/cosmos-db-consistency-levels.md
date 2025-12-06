# Cosmos DB - 5 Consistency Levels Explained

Azure Cosmos DB offers **5 tunable consistency levels** (unique among cloud DBs).

## The 5 Levels (strongest → weakest)

### 1. Strong
- Linearizable reads. Always latest write.
- Higher latency, lower throughput
- Single-region or multi-region with limited write region
- Like traditional RDBMS

### 2. Bounded Staleness
- Reads lag behind writes by at most K versions or T time
- E.g., "reads are at most 5 seconds behind"
- Good for: Global apps needing near-strong consistency

### 3. Session (DEFAULT)
- Consistency within a session (read-your-writes)
- Different sessions may see different versions
- **Most popular choice** — good balance
- Good for: User-facing apps (user sees their own updates)

### 4. Consistent Prefix
- Reads never see out-of-order writes
- No guarantee on recency
- Good for: Social feeds, timelines

### 5. Eventual
- No ordering guarantee, highest performance
- Lowest latency, highest throughput
- Good for: Counters, likes, non-critical analytics

## Cost Impact
Stronger consistency = more RUs consumed per operation.
Strong consistency reads cost 2x compared to eventual.

## Multi-Region
- Strong consistency NOT available with multi-region writes
- Session consistency is the sweet spot for most global apps
