# Azure Cosmos DB Consistency Levels — Deep Dive

## The Five Consistency Levels

Cosmos DB is unique: it offers **five tunable consistency levels** between strong and eventual.

```
Strongest                                                         Weakest
    ├────────────┼────────────┼──────────────┼──────────────┤
  Strong    Bounded      Session       Consistent      Eventual
            Staleness    (default)      Prefix
```

### 1. Strong Consistency
- Reads always return the most recent committed write
- **Linearizable** — equivalent to a single-copy system
- Only available for single-region writes (not multi-region write)
- Highest latency, lowest throughput

**Use case**: Financial transactions, inventory systems

### 2. Bounded Staleness
- Reads may lag behind writes by at most K versions OR T time
- Example: "reads are at most 5 seconds or 100 versions behind"
- Strong consistency with a configurable lag window
- Available in multi-region (unlike Strong)

**Configuration**:
- Single region: K ≥ 10, T ≥ 5 seconds
- Multi-region: K ≥ 100,000, T ≥ 300 seconds

**Use case**: Multi-region apps needing near-strong consistency

### 3. Session Consistency (DEFAULT)
- Within a session (same client/token), reads see that session's writes
- **Read-your-own-writes** guaranteed
- Other sessions might see older data
- Best balance of consistency and performance

**Use case**: User-facing applications (user always sees their own updates)

### 4. Consistent Prefix
- Reads never see out-of-order writes
- If writes are A, B, C — reader sees A, AB, or ABC (never AC or B alone)
- No guarantee on how current the data is

**Use case**: Social feeds, activity logs (order matters, freshness less so)

### 5. Eventual Consistency
- No ordering guarantee
- Reads may return any past version
- Fastest reads, lowest cost
- Typically converges within a few hundred milliseconds

**Use case**: View counts, likes, non-critical analytics

## Performance & Cost Impact

| Level | Read Latency | Write Latency | RU Cost (Read) | Availability |
|-------|-------------|---------------|----------------|-------------|
| Strong | Higher (quorum read) | Normal | 2x | Single-region write |
| Bounded Staleness | Moderate | Normal | 2x | Multi-region |
| Session | Low | Normal | 1x | Multi-region |
| Consistent Prefix | Low | Normal | 1x | Multi-region |
| Eventual | Lowest | Normal | 1x | Multi-region |

**Key insight**: Strong and Bounded Staleness cost **2x RUs** per read because they require quorum reads.

## RU (Request Unit) Explained

```
1 RU = 1 point read of a 1 KB item with Session consistency

Examples:
  Point read (1 KB item)          = 1 RU
  Point read (strong consistency) = 2 RU
  Write (1 KB item)              = 5 RU  (writes are always 5x reads)
  Query (returns 5 items)        = ~10-50 RU (depends on complexity)
  
Pricing: ~$0.008 per 100 RUs (autoscale) in US regions
```

## Choosing the Right Level

```
Do you need linearizable reads?
  ├─ Yes → Strong (single-region only)
  └─ No
      ├─ Multi-region writes needed?
      │   ├─ Yes → Session or Eventual (Strong not available)
      │   └─ No → Any level works
      └─ Users must see their own writes?
          ├─ Yes → Session (default, recommended for 80% of cases)
          └─ No
              ├─ Order matters? → Consistent Prefix
              └─ No → Eventual (cheapest)
```

## Multi-Region Configuration

```
                    Write Region (East US)
                    ┌──────────────────┐
                    │   Cosmos DB      │
                    │   Primary        │
                    └────────┬─────────┘
                             │ Automatic replication
              ┌──────────────┼──────────────┐
              │              │              │
    ┌─────────▼───┐  ┌──────▼──────┐  ┌───▼──────────┐
    │  West US    │  │  West Europe│  │  Southeast   │
    │  (Read)     │  │  (Read)     │  │  Asia (Read) │
    └─────────────┘  └─────────────┘  └──────────────┘
```

- Reads: From nearest region (low latency)
- Writes: To primary region (unless multi-region write enabled)
- Automatic failover if primary goes down

## Cosmos DB vs DynamoDB vs MongoDB

| Feature | Cosmos DB | DynamoDB | MongoDB Atlas |
|---------|----------|----------|--------------|
| Consistency | 5 levels (tunable) | 2 (eventual, strong) | Tunable (causal, majority) |
| Global distribution | Built-in, turnkey | Global Tables | Atlas global clusters |
| Pricing model | RU/s (provisioned/serverless) | WCU/RCU or on-demand | Instance-based |
| APIs | SQL, MongoDB, Cassandra, Gremlin, Table | DynamoDB API | MongoDB API |
| Serverless | ✅ | ✅ | ✅ (Atlas serverless) |
| SLA | 99.999% (multi-region) | 99.999% (global tables) | 99.995% |

## Interview Answer

> "Cosmos DB's five consistency levels are its killer feature. For most applications, I use Session consistency — the default — which guarantees users always see their own writes while keeping costs low (1 RU per read vs 2 RU for strong). For financial data that needs linearizable reads, I'd use Strong consistency, accepting that it limits you to single-region writes. The key trade-off is cost: Strong and Bounded Staleness double your read RU costs. For multi-region deployments, Bounded Staleness gives near-strong guarantees while supporting multi-region writes."
