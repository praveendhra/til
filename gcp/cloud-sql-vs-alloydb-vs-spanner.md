# Cloud SQL vs AlloyDB vs Spanner

## Cloud SQL
Standard managed relational databases.
- Engines: MySQL, PostgreSQL, SQL Server
- Max storage: 64TB
- Max connections: ~4000 (depends on instance)
- HA: Regional (failover replica)
- Read replicas: Up to 10, same region or cross-region
- **Best for**: Traditional apps, simple OLTP, small-medium workloads

## AlloyDB (PostgreSQL-compatible)
High-performance PostgreSQL with custom storage engine.
- 4x faster than standard PostgreSQL (OLTP)
- 100x faster for analytical queries (columnar engine)
- Disaggregated compute + storage
- ML predictions with `google_ml.predict()` SQL function
- **Best for**: PostgreSQL workloads needing more performance, mixed OLTP+OLAP

## Cloud Spanner
Globally distributed relational database.
- Unlimited horizontal scaling
- Strong consistency across regions
- 99.999% SLA (multi-region)
- Expensive ($657/month per node minimum)
- **Best for**: Global apps, financial systems, mission-critical

## Decision Matrix
| Criteria | Cloud SQL | AlloyDB | Spanner |
|----------|-----------|---------|---------|
| Cost | $ | $$ | $$$$ |
| Scale limit | 64TB | 128TB+ | Unlimited |
| Global | No | No | Yes |
| PostgreSQL compatible | Yes | Yes | GoogleSQL or PG |
| Horizontal scaling | No | Limited | Yes |
| Use case | Simple apps | Performance-critical | Global mission-critical |
