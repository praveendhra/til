# Database Replication Models

## 1. Leader-Follower (Primary-Replica)
- One leader handles writes
- Followers replicate and serve reads
- **AWS RDS**, **Azure SQL**, **Cloud SQL** all use this

```
Client --write--> [Leader]
                    |
              [Follower 1] [Follower 2]
Client --read-->
```

**Replication lag**: Followers may be slightly behind. Use `read-after-write consistency` for critical reads.

## 2. Multi-Leader (Active-Active)
- Multiple nodes accept writes
- Conflict resolution needed (last-writer-wins, custom merge)
- **Use case**: Multi-region deployments

**Cloud examples**:
- AWS Aurora Global Database
- Azure Cosmos DB (multi-region writes)
- GCP Spanner

## 3. Leaderless (Dynamo-style)
- Any node accepts reads/writes
- Quorum: W + R > N for consistency
- **Cassandra**, **DynamoDB**, **Riak**

```
Write to W nodes out of N
Read from R nodes out of N
If W + R > N → guaranteed to read latest
```

## Trade-offs
| Model | Consistency | Availability | Complexity |
|-------|------------|-------------|-----------|
| Leader-Follower | Strong (reads from leader) | Leader is SPOF | Low |
| Multi-Leader | Eventual | High | High (conflicts) |
| Leaderless | Tunable | High | Medium |
