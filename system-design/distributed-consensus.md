# Distributed Consensus — Raft, Paxos, and Beyond

## The Problem

How do N servers agree on a value when:
- Messages can be delayed, lost, or reordered
- Servers can crash and restart
- But we assume **no Byzantine faults** (no malicious actors)

This is the core problem behind: leader election, distributed locks, log replication, configuration management.

## Raft — The Understandable Consensus Algorithm

Designed by Diego Ongaro in 2014 as an understandable alternative to Paxos.

### Core Concepts

**Three roles**: Leader, Follower, Candidate

```
Normal operation:
  Leader ──heartbeats──► Followers
  Client ──write──► Leader ──replicate──► Followers ──ack──► Leader ──commit──► Client

Leader election:
  Follower (timeout, no heartbeat) → becomes Candidate
  Candidate → requests votes → majority votes → becomes Leader
```

### Leader Election

```
1. Follower hasn't heard from leader (election timeout: 150-300ms, randomized)
2. Follower → Candidate (increments term, votes for self)
3. Candidate sends RequestVote RPC to all nodes
4. Each node votes for at most ONE candidate per term
5. Candidate with majority → becomes Leader
6. Leader sends heartbeats to suppress new elections
```

**Split vote**: Two candidates start simultaneously, neither gets majority → both timeout with random delay → retry. Randomized timeouts make this rare.

### Log Replication

```
Client sends "SET x=5" to Leader

Leader's log: [term1: SET x=3] [term1: SET y=7] [term2: SET x=5]
                                                     ↑ uncommitted

1. Leader appends entry to its log
2. Leader sends AppendEntries RPC to followers
3. Followers append to their logs, acknowledge
4. Once majority acknowledges → Leader commits entry
5. Leader notifies followers of commit
6. Leader responds to client: success
```

**Safety guarantee**: Once committed, an entry will never be lost (as long as majority survives).

### Where Raft is Used

| System | What It Uses Raft For |
|--------|----------------------|
| **etcd** | Key-value store consensus (used by Kubernetes) |
| **Consul** | Service discovery, KV store |
| **CockroachDB** | Per-range consensus for distributed SQL |
| **TiKV** | Distributed KV storage engine |
| **RabbitMQ** (quorum queues) | Queue replication |

## Paxos — The Original (and Complex) Algorithm

Invented by Leslie Lamport (1989, published 1998). Three roles: Proposer, Acceptor, Learner.

### Basic Paxos (Single-Value)

```
Phase 1: PREPARE
  Proposer → Acceptors: "Prepare(n)" (proposal number n)
  Acceptors → Proposer: "Promise(n)" + any previously accepted value

Phase 2: ACCEPT
  Proposer → Acceptors: "Accept(n, value)"
  Acceptors → Proposer: "Accepted(n, value)"

Commit: When majority of Acceptors accepted → value is chosen
```

### Multi-Paxos

For a sequence of values (log entries), elect a stable leader to skip Phase 1 for subsequent entries. Effectively similar to Raft.

**Used by**: Google Chubby, Google Spanner (internally)

## Comparison

| Property | Raft | Paxos | ZAB (ZooKeeper) |
|----------|------|-------|-----------------|
| Understandability | ✅ Easy | ❌ Hard | Medium |
| Leader required | Yes | No (but Multi-Paxos does) | Yes |
| Consistency | Linearizable | Linearizable | Linearizable |
| Split-brain prevention | Term numbers | Proposal numbers | Epoch numbers |
| Real implementations | etcd, Consul | Chubby, Spanner | ZooKeeper |

## Key Properties (All Guarantee)

1. **Safety**: Never returns wrong result (even under failures)
2. **Liveness**: Eventually makes progress (if majority alive)
3. **Quorum**: Requires majority (N/2 + 1) of nodes
   - 3 nodes → tolerates 1 failure
   - 5 nodes → tolerates 2 failures
   - 7 nodes → tolerates 3 failures (diminishing returns)

## Why 3 or 5 Nodes?

```
Nodes  Quorum  Failures Tolerated
  1      1         0
  2      2         0  ← worse than 1! (need both to agree)
  3      2         1  ← minimum useful
  4      3         1  ← same as 3, but more overhead
  5      3         2  ← sweet spot for production
  7      4         3  ← rarely needed
```

**5 nodes** is the production standard. 3 is minimum for fault tolerance.

## Practical Implications for DevOps

### etcd (Kubernetes)

```yaml
# Production etcd cluster: always odd number of nodes
# 3 nodes for small clusters, 5 for large
etcd-0 (leader)
etcd-1 (follower)
etcd-2 (follower)

# If etcd-0 crashes:
# etcd-1 or etcd-2 holds election → new leader in ~150-300ms
# Kubernetes control plane stays operational
```

**Important**: etcd performance is sensitive to disk latency. Use SSDs.

### Consul

```
# Consul server cluster (3 or 5 nodes)
consul agent -server -bootstrap-expect=3

# Raft consensus for:
# - Service catalog updates
# - KV store writes
# - ACL policy changes
```

## Interview Talking Points

> "Distributed consensus ensures that a cluster of servers agrees on a sequence of values even when some nodes fail. Raft is the most common algorithm today — used by etcd, Consul, and CockroachDB. It works by electing a leader who replicates log entries to followers. Once a majority acknowledges, the entry is committed and can never be lost. The key insight is that you need an odd number of nodes (typically 5 for production) because you need a majority quorum — with 5 nodes, you can tolerate 2 failures while still making progress."

## Common Questions

**Q: What happens during a network partition?**
A: The partition with the majority of nodes continues operating. The minority partition cannot commit new entries (no quorum). When the partition heals, the minority nodes catch up from the leader.

**Q: How fast is leader election?**
A: Typically 150-300ms (Raft election timeout). etcd clusters usually recover in under 1 second.

**Q: Raft vs Paxos?**
A: Equivalent in terms of safety and performance. Raft is designed for understandability and has clearer implementation. Paxos is more theoretically flexible but much harder to implement correctly.

**Q: Why not use 2 nodes?**
A: With 2 nodes, quorum is 2 — both must be alive. That's worse than a single node! Always use odd numbers ≥ 3.
