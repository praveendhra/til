# System Design: Core Principles for Scalable Architecture

This guide breaks down six fundamental concepts essential for designing scalable, resilient systems, as discussed by *Maddy Zhang*.

---

## 1. Statelessness (0:59 - 3:24)
**Horizontal scaling** is only effective if your servers are stateless. 
* **Definition:** The server stores no session data locally. Every request contains all necessary information (usually via an auth token).
* **The Problem with State:** Storing session data (like login state) locally forces *sticky sessions*, where a user must always route to the same server. If that server fails, the data is lost.
* **The Solution:** Offload state to a shared store (e.g., *Redis*). This makes every server interchangeable, enabling flexible load balancing and true fault tolerance.

## 2. Caching (3:24 - 4:22)
Caching is a trade-off between **speed** and **freshness**.
* **Core Strategy:** Store a copy of data closer to the request (e.g., *CDN* for static assets, *Redis* for application-level data).
* **Key Concepts to Master:** 
    * **TTLs (Time To Live):** How long data remains in cache.
    * **Cache-aside:** The application checks the cache first, then the DB.
    * **Write-through/Write-back:** Strategies for synchronizing the cache with the underlying database.

## 3. CAP Theorem (4:22 - 5:35)
In a distributed system, you choose between **Consistency** and **Availability** (as Partition Tolerance is a given constraint).
* **Consistency:** Every read receives the most recent write.
* **Availability:** The system always responds, even if data is not the absolute latest.
* **Pro Tip:** Don't apply this globally. Different parts of your system can have different requirements (e.g., strong consistency for payments, eventual consistency for content feeds).

## 4. Message Queues (5:35 - 6:29)
Queues decouple services, making the system **resilient** to failures.
* **Synchronous flow:** One service calls another; if one dependency fails, the entire chain breaks.
* **Asynchronous flow:** Services publish events (e.g., *Kafka*, *SQS*). Downstream services consume these events at their own pace. If a service is down, the message stays in the queue until the service recovers.

## 5. Databases & ACID (6:29 - 8:17)
The SQL vs. NoSQL debate is really about data guarantees.
* **ACID (SQL):**
    * **Atomicity:** All-or-nothing transactions.
    * **Consistency:** Adherence to defined rules/constraints.
    * **Isolation:** Concurrent transactions don't interfere.
    * **Durability:** Committed data is permanent, even after crashes.
* **NoSQL:** Trades strict ACID guarantees for higher scalability and flexible schemas.

## 6. API Design (8:17 - 9:04)
An API is a **contract** with your users; changing it requires careful coordination.
* **REST:** Simple, cachable, resource-oriented. Great for public APIs.
* **GraphQL:** Flexible; allows clients to request exactly the data they need.
* **Best Practices:** Always prioritize explicit versioning, design around resources, and maintain up-to-date documentation.

---
*"Don't just memorize definitions; build a mental model by asking 'why' for every architectural choice."* — *Maddy Zhang*