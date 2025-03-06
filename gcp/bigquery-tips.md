# BigQuery - Cost Optimization

## Pricing Model
- **On-demand**: $6.25/TB scanned (first 1TB/month free)
- **Flat-rate**: Reserved slots (capacity-based)
- **Storage**: $0.02/GB/month (active), $0.01/GB/month (long-term, >90 days)

## Cost Optimization

### 1. Avoid SELECT *
```sql
-- BAD: Scans all columns ($$$)
SELECT * FROM `project.dataset.events`

-- GOOD: Only scan needed columns
SELECT user_id, event_type, timestamp
FROM `project.dataset.events`
```

### 2. Partition Tables
```sql
CREATE TABLE events
PARTITION BY DATE(timestamp)
CLUSTER BY user_id
AS SELECT * FROM raw_events;

-- Now this only scans relevant partitions
SELECT * FROM events
WHERE DATE(timestamp) = '2025-03-15'
```

### 3. Use Clustering
Co-locate related rows. Up to 4 clustering columns.

### 4. Materialized Views
Pre-compute expensive aggregations:
```sql
CREATE MATERIALIZED VIEW daily_stats AS
SELECT DATE(timestamp) as day, COUNT(*) as events
FROM events
GROUP BY day;
```

### 5. Use `--dry_run` flag
Check bytes scanned before running:
```bash
bq query --dry_run "SELECT * FROM dataset.table"
```

## BigQuery vs Alternatives
| Feature | BigQuery | Redshift | Synapse |
|---------|----------|----------|---------|
| Pricing | Per-scan | Per-node | Per-DWU |
| Serverless | Yes | Serverless option | Serverless option |
| Max scale | Petabytes | Petabytes | Petabytes |
| ML built-in | BQML | Redshift ML | Synapse ML |
