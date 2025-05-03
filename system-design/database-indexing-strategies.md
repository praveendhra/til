# Database Indexing Deep Dive

## B-Tree Index (Default)
- Balanced tree structure, O(log n) lookups
- Good for: equality, range queries, ORDER BY
- PostgreSQL, MySQL default

## Hash Index
- O(1) lookups for equality only
- No range queries, no ordering
- Used in: memory tables, key-value stores

## GIN (Generalized Inverted Index)
- Full-text search, JSONB, arrays
```sql
CREATE INDEX idx_tags ON articles USING GIN (tags);
SELECT * FROM articles WHERE tags @> '{"python", "devops"}';
```

## GiST (Generalized Search Tree)
- Geometric data, range types, full-text
```sql
CREATE INDEX idx_location ON stores USING GiST (location);
SELECT * FROM stores WHERE location <@ circle '((0,0), 10)';
```

## Partial Index
Index only a subset of rows:
```sql
CREATE INDEX idx_active ON users (email) WHERE active = true;
```
Saves space, faster for common queries.

## Composite Index Ordering
```sql
-- For: WHERE status = 'active' AND created_at > '2024-01-01' ORDER BY created_at
CREATE INDEX idx_status_created ON orders (status, created_at DESC);
```
**Rule**: Equality columns first, then range/sort columns.

## Index Anti-Patterns
- Indexing low-cardinality columns (boolean)
- Too many indexes (slow writes)
- Not using EXPLAIN ANALYZE
- Indexing columns rarely queried
