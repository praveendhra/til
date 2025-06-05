# CAP Theorem - You Can Only Pick Two

In any distributed data store, you can only guarantee **two out of three**:

- **Consistency**: Every read receives the most recent write
- **Availability**: Every request receives a response (no errors)
- **Partition Tolerance**: System continues despite network partitions

## Real-World Trade-offs

| System | Choice | Notes |
|--------|--------|-------|
| Traditional RDBMS | CA | Fails under network partition |
| MongoDB | CP | Returns errors when partitioned |
| Cassandra | AP | Eventually consistent |
| DynamoDB | AP (tunable) | Can configure strong consistency |

## Key Insight

In practice, **partition tolerance is non-negotiable** in distributed systems. So the real choice is between **CP** and **AP**.

- **CP**: Banking systems, inventory management
- **AP**: Social media feeds, DNS, caching layers
