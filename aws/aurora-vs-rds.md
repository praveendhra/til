# Aurora vs Standard RDS

## Standard RDS
- MySQL, PostgreSQL, MariaDB, Oracle, SQL Server
- Single primary + up to 5 read replicas
- Storage: EBS-backed, up to 64TB
- Failover: Multi-AZ (synchronous standby)
- Replication lag: seconds to minutes

## Aurora
- MySQL and PostgreSQL compatible (drop-in replacement)
- Custom storage engine: 6 copies across 3 AZs
- Up to 15 read replicas with <10ms lag
- Auto-scaling storage up to 128TB
- 5x throughput of standard MySQL

## Aurora Serverless v2
- Auto-scales compute (0.5 to 256 ACUs)
- Scales in seconds, not minutes
- Pay per ACU-hour (can scale to 0 with v2 pause)
- Great for: variable/unpredictable workloads

## Aurora Global Database
- 1 primary region + up to 5 secondary regions
- <1 second replication lag
- Cross-region failover in <1 minute
- Great for: disaster recovery, low-latency global reads

## When to Choose What
| Scenario | Choice |
|----------|--------|
| Simple app, predictable load | RDS |
| Need Oracle/SQL Server | RDS |
| High throughput, many read replicas | Aurora |
| Variable/spiky workload | Aurora Serverless |
| Global application | Aurora Global Database |
| Cost-sensitive, low traffic | RDS (t3.micro) |
