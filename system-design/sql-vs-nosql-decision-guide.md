# SQL vs NoSQL - When to Use What

## SQL (Relational)
**Choose when**: ACID transactions, complex joins, structured data, strong consistency

| Cloud Service | Engine | Notes |
|--------------|--------|-------|
| AWS RDS | MySQL, PostgreSQL, Oracle | Managed, Multi-AZ |
| AWS Aurora | MySQL/PostgreSQL compatible | 5x faster, serverless option |
| Azure SQL Database | SQL Server | Built-in AI tuning |
| Azure Database for PostgreSQL | PostgreSQL | Flexible server |
| Cloud SQL | MySQL, PostgreSQL, SQL Server | Automatic backups |
| Cloud Spanner | Proprietary | Global, strongly consistent |

## NoSQL

### Key-Value
- **DynamoDB** (AWS), **Cosmos DB** (Azure), **Firestore** (GCP)
- Use for: Session stores, caching, user preferences

### Document
- **MongoDB Atlas**, **Cosmos DB** (MongoDB API), **Firestore**
- Use for: Content management, catalogs, user profiles

### Wide-Column
- **Cassandra**, **HBase**, **Bigtable** (GCP)
- Use for: Time-series, IoT data, analytics at scale

### Graph
- **Neptune** (AWS), **Cosmos DB** (Gremlin), **Neo4j**
- Use for: Social networks, recommendation engines, fraud detection

## Decision Flowchart
1. Need ACID transactions? → **SQL**
2. Need flexible schema? → **Document DB**
3. Need extreme write throughput? → **Wide-Column**
4. Need relationship queries? → **Graph DB**
5. Need simple key lookups at scale? → **Key-Value**
