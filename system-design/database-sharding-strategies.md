# Database Sharding Strategies

Sharding = horizontal partitioning of data across multiple database instances.

## Strategies

### 1. Range-Based Sharding
- Shard by ranges: users A-M → Shard 1, N-Z → Shard 2
- **Pro**: Simple range queries
- **Con**: Hotspots if data isn't evenly distributed

### 2. Hash-Based Sharding
- `shard = hash(key) % num_shards`
- **Pro**: Even distribution
- **Con**: Resharding is painful (use consistent hashing)

### 3. Directory-Based Sharding
- Lookup table maps each key to its shard
- **Pro**: Flexible
- **Con**: Lookup table becomes a bottleneck/SPOF

### 4. Geographic Sharding
- Data stored near users: US → us-east, EU → eu-west
- **Pro**: Low latency, data residency compliance (GDPR)
- **Con**: Cross-region queries are expensive

## When NOT to Shard
- Try **read replicas** first
- Try **vertical scaling** first
- Try **caching** first
- Shard only when you've exhausted simpler options
