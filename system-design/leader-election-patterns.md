# Leader Election in Distributed Systems

## Why?
When only ONE node should perform a task (cron jobs, write coordination, resource management).

## Approaches

### 1. Database-Based
Use a database row as a lock:
```sql
INSERT INTO leaders (service, node_id, expires_at)
VALUES ('scheduler', 'node-1', NOW() + INTERVAL '30 seconds')
ON CONFLICT (service) DO UPDATE
SET node_id = 'node-1', expires_at = NOW() + INTERVAL '30 seconds'
WHERE leaders.expires_at < NOW();
```
Simple but depends on database availability.

### 2. ZooKeeper / etcd / Consul
Create an ephemeral node. First to create it is the leader.
```
# etcd example
etcdctl elect my-service node-1
```
Leader loses election when session expires (crash detection).

### 3. Raft-Based
Built into the consensus protocol. Used by etcd, Consul, CockroachDB.

### 4. Cloud-Native

**AWS**: DynamoDB conditional writes with TTL
```python
# Attempt to become leader
table.put_item(
    Item={'service': 'scheduler', 'leader': node_id, 'ttl': int(time.time()) + 30},
    ConditionExpression='attribute_not_exists(service) OR ttl < :now',
    ExpressionAttributeValues={':now': int(time.time())}
)
```

**Azure**: Blob Storage lease
```csharp
await blob.AcquireLeaseAsync(TimeSpan.FromSeconds(30));
```

**GCP**: Cloud Storage object locking or Pub/Sub ordering keys

## Fencing Tokens
Always use a monotonically increasing token to prevent "zombie leaders" from making stale writes.
