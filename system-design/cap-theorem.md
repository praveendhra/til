# CAP Theorem — Deep Dive for Interviews

## The Theorem (Eric Brewer, 2000)

In a **distributed data store**, you can only guarantee **two out of three**:

| Property | Definition |
|----------|-----------|
| **Consistency (C)** | Every read receives the most recent write or an error. All nodes see the same data at the same time. |
| **Availability (A)** | Every request receives a non-error response, without guarantee it contains the most recent write. |
| **Partition Tolerance (P)** | The system continues to operate despite an arbitrary number of messages being dropped or delayed by the network. |

## Why You Actually Choose Between C and A

In any real distributed system, **network partitions will happen** (hardware failures, network congestion, cloud AZ issues). So P is non-negotiable. The real choice is:

```
           Partition Happens
                 |
        +--------+--------+
        |                 |
   Choose C (CP)     Choose A (AP)
   Block/error        Serve stale
   until consistent   but stay up
```

### CP Systems — "I'd rather be correct than available"
- When a partition occurs, nodes that can't confirm they have the latest data will **refuse to serve reads**
- Example: Banking transactions, inventory counts, leader election
- Systems: **ZooKeeper**, **etcd**, **HBase**, **MongoDB** (default), **Redis Cluster** (in certain configs)

### AP Systems — "I'd rather be available than correct"
- When a partition occurs, every node continues serving requests using whatever data it has
- Uses **eventual consistency** — data converges once the partition heals
- Example: Shopping cart, social media feeds, DNS, CDN caches
- Systems: **Cassandra**, **DynamoDB** (default), **CouchDB**, **Riak**

## Real-World Database Classification

| Database | Default | Tunable? | Notes |
|----------|---------|----------|-------|
| PostgreSQL (single) | CA | No | No partition tolerance — single node |
| PostgreSQL + Patroni | CP | Partially | Failover may cause brief unavailability |
| MongoDB | CP | Yes | `w:majority, r:majority` = strong; `w:1` = eventual |
| Cassandra | AP | Yes | `QUORUM` reads/writes give stronger consistency |
| DynamoDB | AP | Yes | `ConsistentRead=true` for strong reads |
| CockroachDB | CP | No | Serializable by default, inspired by Spanner |
| Redis Sentinel | CP | No | Failover = brief downtime |
| etcd | CP | No | Raft consensus, used by Kubernetes |
| Consul | CP | Yes | CP for KV store, AP for service discovery |

## PACELC — The Extended Model

CAP only describes behavior **during** a partition. **PACELC** (Daniel Abadi, 2012) adds:

> If there is a **P**artition, choose **A** or **C**;  
> **E**lse (normal operation), choose **L**atency or **C**onsistency.

| System | Partition (PAC) | Normal (ELC) |
|--------|----------------|-------------|
| DynamoDB | PA | EL (fast, eventual) |
| Cassandra | PA | EL |
| MongoDB | PC | EC (consistent, higher latency) |
| CockroachDB | PC | EC |
| Cosmos DB | PA/PC (tunable) | EL/EC (tunable via consistency levels) |

## Consistency Models Spectrum

```
Strongest ◄─────────────────────────────────────────────► Weakest

Linearizable → Sequential → Causal → Read-your-writes → Eventual
     │              │          │              │              │
  Spanner      ZooKeeper   DynamoDB       Session      Cassandra
  CockroachDB              Streams      consistency    (ONE/ONE)
```

**Linearizability**: Operations appear to happen atomically at some point between invocation and response. The gold standard.

**Eventual Consistency**: If no new updates, all replicas converge. No guarantee when. Could be milliseconds or seconds.

## How to Explain in an Interview

> "CAP theorem says that in a distributed system, when a network partition occurs — which is inevitable — you have to choose between consistency and availability. Most modern databases let you tune this trade-off per query. For example, DynamoDB defaults to eventually consistent reads for lower latency, but you can opt into strongly consistent reads when correctness matters. The key insight is understanding your application's requirements: a payment system needs CP, while a social feed can tolerate AP."

## Common Follow-up Questions

**Q: Is CAP theorem still relevant?**
A: Yes, but PACELC is more nuanced. Also, Martin Kleppmann argues CAP oversimplifies — real systems have more nuanced consistency/availability spectrums.

**Q: Can you have all three?**
A: Only if there are no partitions (single node). In practice, no distributed system achieves all three simultaneously.

**Q: How does Google Spanner claim to be CA?**
A: Spanner uses TrueTime (atomic clocks + GPS) to keep the partition window so small that partitions are extremely rare. It's technically CP but practically behaves like CA due to Google's network reliability.

**Q: What consistency level should I use for Cassandra?**
A: `QUORUM` for both reads and writes gives you strong consistency: `W + R > N` (e.g., with RF=3, QUORUM=2, so 2+2 > 3). `ONE/ONE` gives AP behavior.

## References
- Original paper: "Brewer's Conjecture and the Feasibility of Consistent, Available, Partition-Tolerant Web Services" (Gilbert & Lynch, 2002)
- "Please stop calling databases CP or AP" — Martin Kleppmann
- PACELC: "Consistency Tradeoffs in Modern Distributed Database System Design" — Abadi, 2012
