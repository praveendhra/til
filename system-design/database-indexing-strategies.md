# Database Indexing — Complete Guide

## What Is an Index?

A data structure that speeds up lookups at the cost of storage and write performance.

```
Without index: Full table scan → O(n)
With B-Tree index: Binary search → O(log n)
With Hash index: Direct lookup → O(1)

Table with 10M rows:
  Full scan: ~10 seconds
  B-Tree index: ~0.01 seconds (1000x faster!)
```

## Index Types

### B-Tree Index (Default, Most Common)

A balanced tree structure. Each node contains sorted keys and pointers.

```
                    [50]
                   /    \
              [20,35]   [65,80]
             /  |  \   /  |  \
          [10] [25] [40] [55] [70] [90]
```

**Good for**: Equality (`=`), range (`<`, `>`, `BETWEEN`), sorting (`ORDER BY`), prefix matching (`LIKE 'abc%'`)
**Bad for**: Full-text search, suffix matching (`LIKE '%abc'`)

### Hash Index

Direct hash table lookup. O(1) for equality.

**Good for**: Exact equality only (`=`)
**Bad for**: Range queries, sorting, `LIKE`
**Available in**: PostgreSQL (explicitly), some engines use internally

### GIN (Generalized Inverted Index)

Maps each value to the rows containing it. Like a book's index.

**Good for**: Full-text search, JSONB containment, array operations
**PostgreSQL**: `CREATE INDEX idx ON docs USING gin(to_tsvector('english', content));`

### GiST (Generalized Search Tree)

**Good for**: Geometric data, range types, full-text search
**Use case**: PostGIS spatial queries, nearest-neighbor searches

## Composite (Multi-Column) Indexes

```sql
CREATE INDEX idx_user_status_date
ON orders (user_id, status, created_at);
```

### The Left-Prefix Rule

A composite index on `(A, B, C)` can be used for:
- ✅ `WHERE A = ?`
- ✅ `WHERE A = ? AND B = ?`
- ✅ `WHERE A = ? AND B = ? AND C = ?`
- ✅ `WHERE A = ? AND B > ?`
- ❌ `WHERE B = ?` (skips A — can't use index)
- ❌ `WHERE A = ? AND C = ?` (skips B — only uses A portion)
- ⚠️ `WHERE A = ? AND B > ? AND C = ?` (B uses range, C can't use index after range)

### Column Order Matters!

```sql
-- Query: WHERE status = 'active' AND user_id = 123

-- ✅ Good: High cardinality column first
CREATE INDEX idx ON orders (user_id, status);
-- user_id narrows to ~100 rows, then status filters further

-- ❌ Less optimal: Low cardinality first
CREATE INDEX idx ON orders (status, user_id);
-- status narrows to ~1M rows (many 'active'), then user_id filters
```

**Rule of thumb**: Put the most selective (highest cardinality) column first, unless you're optimizing for a specific query pattern.

## Covering Index (Index-Only Scan)

An index that includes all columns needed by a query. The DB never reads the table.

```sql
-- Query:
SELECT user_id, email FROM users WHERE user_id = 123;

-- Covering index (PostgreSQL):
CREATE INDEX idx_covering ON users (user_id) INCLUDE (email);
-- or composite:
CREATE INDEX idx_covering ON users (user_id, email);

-- The DB reads the answer directly from the index → no table lookup!
```

## Partial Index (Filtered Index)

Index only a subset of rows.

```sql
-- Only index active orders (90% of queries are for active orders)
CREATE INDEX idx_active_orders ON orders (created_at)
WHERE status = 'active';

-- Much smaller index, faster writes for inactive orders
```

## When NOT to Index

| Scenario | Why |
|----------|-----|
| Small tables (< 1000 rows) | Full scan is fast enough |
| Columns rarely in WHERE/JOIN | Index overhead for no benefit |
| Columns with very low cardinality | Boolean: only 2 values, index doesn't help much |
| Write-heavy tables (high INSERT/UPDATE rate) | Indexes slow down writes |
| Wide tables with many indexes | Each index doubles write cost |

## Index Overhead

```
Each index costs:
  - Storage: ~10-30% of table size per index
  - Write speed: Each INSERT updates ALL indexes
  - Maintenance: VACUUM/REINDEX operations

Table: 1GB, 5 indexes
  Total storage: ~2.5 GB (table + indexes)
  INSERT speed: ~5x slower than no indexes
```

## EXPLAIN ANALYZE — Your Best Friend

```sql
-- Always check if your index is being used!
EXPLAIN ANALYZE SELECT * FROM orders WHERE user_id = 123;

-- Output:
Index Scan using idx_orders_user_id on orders
  (cost=0.43..8.45 rows=1 width=100)
  (actual time=0.023..0.025 rows=1 loops=1)
Planning Time: 0.150 ms
Execution Time: 0.050 ms

-- ❌ Bad sign:
Seq Scan on orders    ← Full table scan! Index not used
  Filter: (user_id = 123)
  Rows Removed by Filter: 9999999
```

### Why PostgreSQL Might Not Use Your Index

1. **Stale statistics**: Run `ANALYZE tablename;`
2. **Too many rows match**: If query returns > ~10-20% of table, seq scan is faster
3. **Wrong column order** in composite index
4. **Type mismatch**: `WHERE id = '123'` when id is INTEGER
5. **Function on column**: `WHERE LOWER(email) = 'a@b.com'` — create functional index

## Functional/Expression Index

```sql
-- Index on a function result
CREATE INDEX idx_lower_email ON users (LOWER(email));

-- Now this query uses the index:
SELECT * FROM users WHERE LOWER(email) = 'john@example.com';
```

## Interview Answer

> "I design indexes based on actual query patterns using EXPLAIN ANALYZE. For composite indexes, column order matters — I put the most selective column first and follow the left-prefix rule. I use partial indexes to index only relevant rows (e.g., active orders) which keeps the index small and writes fast. For read-heavy queries, covering indexes with INCLUDE avoid table lookups entirely. The trade-off is always write performance vs read performance — each index slows down INSERT/UPDATE, so I aim for the minimum number of indexes that cover the critical query paths."
