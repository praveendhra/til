# Consistent Hashing — Deep Dive

## The Problem

Imagine you have N cache servers and you hash keys with `hash(key) % N`.

```
Server 0: keys where hash % 3 == 0
Server 1: keys where hash % 3 == 1
Server 2: keys where hash % 3 == 2
```

**What happens when you add Server 3?** Almost every key remaps → **massive cache invalidation** (cache storm).

With N=3→4 servers, approximately **75%** of keys need to move. Formula: `(N-1)/N` keys move.

## The Solution: Consistent Hashing

Place servers and keys on a **virtual ring** (hash space 0 to 2³² - 1):

```
                    0
                  ╱   ╲
               S₁       S₂
              ╱             ╲
           key₃              key₁
            │                 │
           key₄              key₂
              ╲             ╱
               S₃       S₄
                  ╲   ╱
                   2³²
```

**Rule**: Each key is assigned to the **first server found clockwise** on the ring.

### When a server is added:
Only keys between the new server and its predecessor need to move → **K/N keys** move on average (K = total keys, N = servers).

### When a server is removed:
Only keys from that server move to the next server clockwise.

## Virtual Nodes (vnodes) — Critical for Production

**Problem**: With few physical nodes, the ring has uneven distribution. One server might get 60% of keys.

**Solution**: Each physical server maps to **multiple positions** (virtual nodes) on the ring.

```
Physical Server A → vnode_A_0, vnode_A_1, vnode_A_2, ..., vnode_A_149
Physical Server B → vnode_B_0, vnode_B_1, vnode_B_2, ..., vnode_B_149
```

Typical count: **100-200 vnodes per physical server**. More vnodes = better distribution but more memory for the ring.

### Benefits of vnodes:
1. **Uniform distribution**: Keys spread more evenly
2. **Heterogeneous hardware**: Powerful servers get more vnodes
3. **Faster rebalancing**: When a node goes down, load spreads across many nodes instead of one

## Real-World Usage

| System | How It Uses Consistent Hashing |
|--------|-------------------------------|
| **Amazon DynamoDB** | Partition keys mapped to storage nodes; vnodes for even distribution |
| **Apache Cassandra** | Token ring; each node owns a range of tokens; vnodes (default 256 per node) |
| **Memcached clients** | Client-side consistent hashing (ketama algorithm) for server selection |
| **Akamai CDN** | Original paper (1997) — mapping content to edge servers |
| **Discord** | Routing messages to the correct server/guild |
| **Redis Cluster** | 16384 hash slots distributed across nodes |

## Implementation Sketch (Python)

```python
import hashlib
from bisect import bisect_right

class ConsistentHash:
    def __init__(self, nodes=None, vnodes=150):
        self.vnodes = vnodes
        self.ring = {}          # hash -> node
        self.sorted_keys = []   # sorted hash values
        for node in (nodes or []):
            self.add_node(node)

    def _hash(self, key: str) -> int:
        return int(hashlib.md5(key.encode()).hexdigest(), 16)

    def add_node(self, node: str):
        for i in range(self.vnodes):
            h = self._hash(f"{node}:{i}")
            self.ring[h] = node
            self.sorted_keys.append(h)
        self.sorted_keys.sort()

    def remove_node(self, node: str):
        for i in range(self.vnodes):
            h = self._hash(f"{node}:{i}")
            del self.ring[h]
            self.sorted_keys.remove(h)

    def get_node(self, key: str) -> str:
        if not self.ring:
            return None
        h = self._hash(key)
        idx = bisect_right(self.sorted_keys, h) % len(self.sorted_keys)
        return self.ring[self.sorted_keys[idx]]
```

## Hash Functions Choice

| Hash Function | Speed | Distribution | Use Case |
|--------------|-------|-------------|----------|
| MD5 | Medium | Excellent | Cassandra, general use |
| MurmurHash3 | Fast | Excellent | Redis Cluster, Guava |
| xxHash | Very Fast | Excellent | High-throughput systems |
| SHA-1 | Slow | Excellent | When crypto-grade needed |

**Don't use**: CRC32 (poor distribution), simple modulo (not consistent)

## Handling Replication

In systems like Cassandra with replication factor RF=3:

```
Ring: ... → Node A → Node B → Node C → Node D → ...
Key K hashes to Node A

Replicas stored on:
  - Node A (primary)
  - Node B (first clockwise)
  - Node C (second clockwise)
```

This ensures replicas are on different physical nodes. With rack-awareness, you skip nodes in the same rack.

## Interview Talking Points

> "Consistent hashing solves the problem of distributing data across a dynamic set of servers where servers can be added or removed with minimal disruption. The key insight is that instead of `hash % N` — which remaps almost everything when N changes — you map both keys and servers onto a ring, so only keys near a changed server need to move. Virtual nodes solve the hotspot problem by giving each physical server many positions on the ring. This is how DynamoDB, Cassandra, and most CDNs work under the hood."

## Common Follow-ups

**Q: How many vnodes should I use?**
A: Cassandra uses 256 by default. More vnodes = better distribution but more metadata. 100-300 is typical.

**Q: What happens during a network partition?**
A: Depends on the system's consistency model. In Cassandra (AP), the remaining nodes take over the ring range. In a CP system, those keys become unavailable.

**Q: How do you handle hotkeys (celebrity problem)?**
A: Consistent hashing doesn't help with hotkeys. You need application-level solutions: caching layer, key splitting (append random suffix), or dedicated servers for hot partitions.
