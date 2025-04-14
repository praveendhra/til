# Cloud Spanner

## What Makes It Unique
- **Relational** (SQL, schemas, ACID transactions)
- **Globally distributed** (automatic replication across regions)
- **Strongly consistent** (not eventually consistent like most distributed DBs)
- **Horizontally scalable** (just add nodes)

This was thought impossible (CAP theorem). Spanner achieves it using **TrueTime** (atomic clocks + GPS in every datacenter).

## When to Use Spanner
- Global financial systems needing strong consistency
- Gaming leaderboards with millions of writes/sec
- Supply chain management across regions
- Any workload needing SQL + horizontal scale + global distribution

## Pricing (Expensive!)
- Node: ~$0.90/hr (~$657/month)
- Storage: $0.30/GB/month
- Minimum: 1 node (handles ~10K reads/sec or 2K writes/sec)
- **Not for small apps** — start with Cloud SQL, graduate to Spanner

## Schema Design
```sql
CREATE TABLE Users (
  UserId    INT64 NOT NULL,
  Name      STRING(MAX),
  Email     STRING(MAX),
) PRIMARY KEY (UserId);

-- Interleave child table for co-location
CREATE TABLE Orders (
  UserId    INT64 NOT NULL,
  OrderId   INT64 NOT NULL,
  Amount    FLOAT64,
) PRIMARY KEY (UserId, OrderId),
  INTERLEAVE IN PARENT Users ON DELETE CASCADE;
```

**Anti-pattern**: Sequential primary keys (use UUID or bit-reversed sequence to avoid hotspots)

## Comparison
| Feature | Spanner | Aurora | Cosmos DB |
|---------|---------|--------|-----------|
| SQL | Full SQL | MySQL/PostgreSQL | SQL API (limited) |
| Global distribution | Yes | Global DB option | Yes |
| Consistency | Strong | Strong (single region) | 5 levels |
| Auto-scaling | Yes | Yes (serverless) | Yes |
