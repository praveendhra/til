# Database Sharding Strategies

## What Is Sharding?
Horizontal partitioning of data across multiple database instances to distribute load.

## Sharding Strategies

### 1. Range-Based Sharding
```
Shard 1: user_id 1-1M
Shard 2: user_id 1M-2M
Shard 3: user_id 2M-3M
```
- Simple to implement
- Risk of hotspots (recent data is always on last shard)

### 2. Hash-Based Sharding
```
shard = hash(user_id) % num_shards
```
- Even distribution
- Hard to add shards (use consistent hashing)

### 3. Directory-Based Sharding
```
Lookup table: user_id → shard_id
```
- Flexible routing
- Lookup table becomes bottleneck/SPOF

### 4. Geographic Sharding
```
US users → us-east shard
EU users → eu-west shard
```
- Lower latency for users
- Complex cross-region queries

## Challenges
- **Cross-shard queries**: JOINs across shards are expensive
- **Rebalancing**: Moving data between shards
- **Referential integrity**: No foreign keys across shards
- **Auto-increment IDs**: Need distributed ID generation (Snowflake)

## When NOT to Shard
- Single server can handle the load
- Read replicas can solve read scaling
- Vertical scaling is still an option
- Data size < 1TB
