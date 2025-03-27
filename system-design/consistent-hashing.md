# Consistent Hashing

## Problem
When using `hash(key) % N` for N servers, adding/removing a server remaps almost all keys.

## Solution
Arrange servers on a hash ring. Each key maps to the next server clockwise on the ring.

## How It Works
1. Hash servers onto a ring (0 to 2^32-1)
2. Hash each key onto the same ring
3. Walk clockwise from key's position to find the server

## Virtual Nodes
Real servers map to multiple points on the ring:
```
Server A → vnode_A_0, vnode_A_1, vnode_A_2, ...
Server B → vnode_B_0, vnode_B_1, vnode_B_2, ...
```

Benefits:
- More even distribution of keys
- Smoother rebalancing when nodes join/leave
- Typically 100-200 vnodes per physical node

## Impact of Node Changes
| Operation | Keys Remapped |
|-----------|---------------|
| Traditional (mod N) | ~100% |
| Consistent hashing | ~K/N (only affected keys) |

## Used In
- Amazon DynamoDB
- Apache Cassandra
- Memcached
- Redis Cluster
- Content Delivery Networks (CDNs)
