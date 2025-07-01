# Consistent Hashing for Distributed Systems

Traditional hashing (`hash(key) % N`) breaks when you add/remove servers — nearly all keys get remapped.

## How Consistent Hashing Works

1. Map both **servers** and **keys** onto a circular hash ring (0 to 2^32)
2. Each key is assigned to the **next server clockwise** on the ring
3. Adding/removing a server only affects keys between it and its predecessor

## Virtual Nodes

To handle uneven distribution, each physical server gets multiple **virtual nodes** on the ring.

```
Server A → vnode_A1, vnode_A2, vnode_A3, ...
Server B → vnode_B1, vnode_B2, vnode_B3, ...
```

## Used By

- **Amazon DynamoDB** — partition key routing
- **Apache Cassandra** — token ring
- **Memcached** — client-side consistent hashing
- **Content Delivery Networks** — cache distribution
