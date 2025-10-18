# DynamoDB Single-Table Design Patterns

## Why Single-Table?
- One table serves all access patterns
- Minimize round trips (no JOINs in NoSQL)
- Cost efficient (pay per request)

## Key Concepts
- **Partition Key (PK)**: Distribution key. Must be high-cardinality.
- **Sort Key (SK)**: Range queries within a partition.
- **GSI**: Global Secondary Index — alternative access pattern.

## Common Patterns

### Entity Pattern
```
PK              | SK              | Data
USER#123        | PROFILE         | {name, email, ...}
USER#123        | ORDER#001       | {total, status, ...}
USER#123        | ORDER#002       | {total, status, ...}
ORG#456         | METADATA        | {name, plan, ...}
ORG#456         | MEMBER#123      | {role, joined, ...}
```

### Query: Get user + all orders
```
PK = "USER#123" AND SK begins_with("ORDER#")
```

### GSI for Inverse Lookups
```
GSI1PK          | GSI1SK          | Purpose
ORDER#001       | USER#123        | Find order by ID
EMAIL#a@b.com   | USER#123        | Find user by email
```

## Best Practices
- **Avoid hot partitions**: Don't use timestamps as PK
- **Use write sharding** for high-throughput counters: `COUNTER#1`, `COUNTER#2`, ...
- **TTL**: Set `expiresAt` for auto-deletion (free!)
- **DynamoDB Streams**: Capture changes for CDC/event-driven
