# Database Sharding — Complete Guide

## What Is Sharding?

Splitting a single database into multiple smaller databases (**shards**), each holding a subset of the data. Each shard runs on its own server.

```
Before:  One DB with 1TB data, 10K QPS
After:   4 shards × 250GB each, ~2.5K QPS each
```

## When Do You Need Sharding?

**Signs you need it:**
- Single database CPU consistently > 80%
- Write throughput hitting IOPS limits
- Data size approaching single-machine storage/memory limits
- Replication lag growing on read replicas
- Vertical scaling costs becoming prohibitive (the $50K/month server)

**Try these first:**
1. Read replicas (handles read scaling)
2. Caching layer (Redis/Memcached)
3. Query optimization (indexes, query rewriting)
4. Vertical scaling (bigger instance)
5. Connection pooling (PgBouncer)
6. Table partitioning (single DB, multiple partitions)

## Sharding Strategies

### 1. Range-Based Sharding

Partition by key ranges: users 1-1M on shard 1, 1M-2M on shard 2, etc.

```
Shard 1: user_id [1 - 999,999]
Shard 2: user_id [1,000,000 - 1,999,999]
Shard 3: user_id [2,000,000 - 2,999,999]
```

**Pros**: Simple, efficient range queries, easy to understand
**Cons**: Hotspots if recent users are most active (all hits go to latest shard)
**Good for**: Time-series data (shard by month), archival data

### 2. Hash-Based Sharding

`shard_id = hash(shard_key) % num_shards`

```
hash("user_123") % 4 = 2 → Shard 2
hash("user_456") % 4 = 0 → Shard 0
```

**Pros**: Even distribution, no hotspots (with good hash function)
**Cons**: Range queries span all shards (scatter-gather), resharding is painful
**Good for**: User data, session data, most OLTP workloads

### 3. Directory-Based Sharding

A lookup service/table maps each key to its shard.

```
Lookup Table:
  user_123 → shard_2
  user_456 → shard_0
  tenant_A → shard_1
```

**Pros**: Flexible, can move individual keys between shards
**Cons**: Lookup service is a SPOF, adds latency
**Good for**: Multi-tenant SaaS (tenant-based sharding)

### 4. Geographic Sharding

Data sharded by user location.

```
Shard US-East: Users in Americas
Shard EU-West: Users in Europe
Shard AP-South: Users in Asia-Pacific
```

**Pros**: Low latency (data near users), data residency compliance (GDPR)
**Cons**: Uneven distribution, cross-region queries are expensive
**Good for**: Global applications, compliance-driven architectures

## Choosing a Shard Key

The shard key is the **most important decision**. It determines:
- Data distribution (even vs. skewed)
- Query routing (single-shard vs. scatter-gather)
- Join feasibility (co-located vs. cross-shard)

### Good Shard Key Properties:
1. **High cardinality**: Many distinct values (user_id ✓, country ✗)
2. **Even distribution**: No hotspots (user_id ✓, celebrity_id ✗)
3. **Query isolation**: Most queries hit one shard (tenant_id for SaaS ✓)
4. **Immutable**: Doesn't change (user_id ✓, email ✗)

### Examples:

| Application | Good Shard Key | Why |
|-------------|---------------|-----|
| Multi-tenant SaaS | tenant_id | All tenant data on one shard, natural isolation |
| Social media | user_id | User's posts, followers on same shard |
| E-commerce | order_id (hash) | Even distribution, orders are independent |
| Chat app | channel_id | All messages in a channel co-located |
| Time-series | time_bucket + source | Balanced writes, efficient time-range queries |

## The Hard Problems

### 1. Cross-Shard Queries
```sql
-- This becomes VERY expensive with sharding:
SELECT * FROM orders
JOIN products ON orders.product_id = products.id
WHERE orders.user_id = 123;

-- If orders is sharded by user_id but products is on a different shard...
-- You need a scatter-gather or denormalization
```

**Solutions:**
- **Denormalize**: Store product info in the orders table
- **Reference tables**: Small tables replicated to all shards (countries, categories)
- **Application-level joins**: Fetch from each shard, join in application

### 2. Cross-Shard Transactions
ACID transactions across shards require **2-Phase Commit (2PC)** → slow, complex, fragile.

**Solutions:**
- Design schema so transactions stay within one shard
- Use **Saga pattern** for distributed workflows
- Accept eventual consistency where possible

### 3. Resharding (Adding/Removing Shards)

When you go from 4 → 8 shards, data needs to move.

**Approach 1: Consistent Hashing** (best)
- Only K/N keys move (K=keys, N=new total shards)
- Used by DynamoDB, Cassandra

**Approach 2: Double-write migration**
1. Write to both old and new shard layout
2. Backfill new shards from old
3. Verify consistency
4. Switch reads to new layout
5. Stop writes to old layout

### 4. Auto-Increment IDs
`AUTO_INCREMENT` doesn't work across shards (conflicts).

**Solutions:**
- **UUIDs**: No coordination needed, but large (128 bits), not sortable
- **Snowflake IDs** (Twitter): 64-bit, sortable, contains timestamp
  ```
  | 41 bits: timestamp | 10 bits: machine ID | 12 bits: sequence |
  ```
- **ULID**: Like UUID but sortable, 128-bit
- **Database sequences**: Each shard has offset (shard1: 1,5,9; shard2: 2,6,10; shard3: 3,7,11)

## Sharding at Scale — Real Examples

### Instagram (early days)
- Sharded PostgreSQL by user_id
- Used PL/pgSQL for shard routing
- Each logical shard mapped to a physical PostgreSQL schema
- Multiple logical shards per physical server → easy resharding

### Vitess (YouTube/PlanetScale)
- Sharding middleware for MySQL
- Transparent to application (looks like one DB)
- Handles cross-shard queries, resharding
- Used by Slack, HubSpot, GitHub

### CockroachDB / TiDB
- Automatic sharding built into the database
- Application doesn't need to know about shards
- Trade-off: Higher per-query latency due to distributed coordination

## Architecture Pattern

```
Application Servers
        │
   ┌────┴────┐
   │  Shard  │ (routing layer)
   │  Router │
   └────┬────┘
   ┌────┼────┬────┐
   │    │    │    │
 Shard Shard Shard Shard
  0     1     2     3
(each with its own replica set)
```

## Interview Talking Points

> "Sharding is a last resort for horizontal scaling — I'd exhaust read replicas, caching, and query optimization first. When sharding is needed, the shard key choice is critical: it should have high cardinality, even distribution, and keep related queries on the same shard. For a multi-tenant SaaS app, tenant_id is ideal because it provides natural data isolation. The hardest challenges are cross-shard joins (solved by denormalization), distributed transactions (solved by Saga pattern), and resharding (solved by consistent hashing or tools like Vitess)."
