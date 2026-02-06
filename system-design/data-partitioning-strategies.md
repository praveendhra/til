# Data Partitioning in Distributed Databases

## Why Partition?
Single node can't handle all data. Split data across nodes for:
- **Scalability**: More nodes = more capacity
- **Performance**: Parallel processing
- **Availability**: Failure of one partition doesn't affect others

## Horizontal vs Vertical

### Horizontal (Sharding)
Split rows across nodes. User 1-1M → Node A, 1M-2M → Node B.

### Vertical
Split columns. User profile → Node A, User activity → Node B.

## Cloud-Native Partitioning

### DynamoDB
- Partition key determines physical partition
- Choose high-cardinality keys to avoid hot partitions
- Bad: `date` as partition key (today gets all writes)
- Good: `user_id` as partition key

### Cosmos DB
- Logical partition = partition key value
- Physical partition = auto-managed (up to 50GB per logical partition)
- Cross-partition queries are expensive

### Cloud Spanner
- Primary key determines split boundaries
- Auto-splits based on load and size
- Interleaved tables for co-locating parent-child data

### Azure SQL
- Elastic pools for multi-tenant partitioning
- Shard map manager for routing queries
