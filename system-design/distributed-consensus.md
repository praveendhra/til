# Distributed Consensus - Raft and Paxos

## The Problem
How do multiple nodes agree on a value when nodes can fail and messages can be lost?

## Raft (Understandable Consensus)
Used by: **etcd** (Kubernetes), **CockroachDB**, **Consul**

### Leader Election
1. Nodes start as **followers**
2. If no heartbeat from leader → follower becomes **candidate**
3. Candidate requests votes from peers
4. Majority votes → becomes **leader**
5. Leader sends heartbeats to maintain authority

### Log Replication
1. Client sends write to leader
2. Leader appends to its log
3. Leader replicates to followers
4. Once **majority** acknowledges → committed
5. Leader responds to client

## Paxos
More general but harder to understand. Used by **Google Spanner**, **Chubby**.

## In Practice
You rarely implement consensus directly. Instead use:
- **etcd**: Key-value store with Raft (powers Kubernetes)
- **ZooKeeper**: Coordination service with ZAB protocol
- **Consul**: Service mesh + KV store with Raft

## Cloud Equivalents
- AWS: **DynamoDB** (internally uses Paxos-like)
- Azure: **Cosmos DB** (multi-Paxos for global distribution)
- GCP: **Spanner** (TrueTime + Paxos for global strong consistency)
