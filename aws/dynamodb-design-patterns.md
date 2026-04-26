# DynamoDB Design Patterns — Interview Guide

## Core Concepts

DynamoDB is a **fully managed NoSQL** key-value and document database. Understanding its access patterns is critical.

### Primary Key Types

```
1. Simple Primary Key (Partition Key only):
   PK: "USER#123"

2. Composite Primary Key (Partition Key + Sort Key):
   PK: "USER#123"  SK: "ORDER#2024-001"
```

### Capacity Modes

| Mode | Pricing | Best For |
|------|---------|----------|
| **On-Demand** | Pay per request ($1.25/M writes, $0.25/M reads) | Unpredictable traffic, new apps |
| **Provisioned** | Pay for provisioned RCU/WCU | Predictable traffic (cheaper at scale) |

```
1 WCU = 1 write/sec for items up to 1 KB
1 RCU = 1 strongly consistent read/sec for items up to 4 KB
     = 2 eventually consistent reads/sec for items up to 4 KB
```

## Single-Table Design

The core DynamoDB pattern: **one table serves ALL access patterns**.

### Why?
- No JOINs in DynamoDB — everything must be in one query
- Minimize round trips to the database
- Efficient use of provisioned capacity

### Example: E-Commerce

```
Access patterns:
  1. Get user profile
  2. Get all orders for a user
  3. Get specific order
  4. Get all items in an order
  5. Get user by email (GSI)

Table Design:
┌──────────────────┬────────────────────┬─────────────────────┐
│ PK               │ SK                 │ Attributes          │
├──────────────────┼────────────────────┼─────────────────────┤
│ USER#u001        │ PROFILE            │ name, email, plan   │
│ USER#u001        │ ORDER#2024-001     │ total, status, date │
│ USER#u001        │ ORDER#2024-002     │ total, status, date │
│ ORDER#2024-001   │ ITEM#sku-abc       │ qty, price, name    │
│ ORDER#2024-001   │ ITEM#sku-def       │ qty, price, name    │
│ ORDER#2024-001   │ METADATA           │ shipping, payment   │
├──────────────────┼────────────────────┼─────────────────────┤
│ GSI1PK           │ GSI1SK             │                     │
├──────────────────┼────────────────────┼─────────────────────┤
│ EMAIL#a@b.com    │ USER#u001          │ (lookup user by email)
│ STATUS#SHIPPED   │ ORDER#2024-001     │ (orders by status)  │
└──────────────────┴────────────────────┴─────────────────────┘
```

### Query Examples

```python
# Get user profile
table.get_item(Key={"PK": "USER#u001", "SK": "PROFILE"})

# Get all orders for user
table.query(
    KeyConditionExpression="PK = :pk AND begins_with(SK, :sk)",
    ExpressionAttributeValues={":pk": "USER#u001", ":sk": "ORDER#"}
)

# Get all items in an order
table.query(
    KeyConditionExpression="PK = :pk AND begins_with(SK, :sk)",
    ExpressionAttributeValues={":pk": "ORDER#2024-001", ":sk": "ITEM#"}
)

# Get user by email (GSI)
table.query(
    IndexName="GSI1",
    KeyConditionExpression="GSI1PK = :email",
    ExpressionAttributeValues={":email": "EMAIL#a@b.com"}
)
```

## Advanced Patterns

### Write Sharding (Hot Partition Prevention)
For high-write items (counters, leaderboards), distribute across multiple partitions:

```python
import random

def increment_counter(item_id, num_shards=10):
    shard = random.randint(0, num_shards - 1)
    table.update_item(
        Key={"PK": f"COUNTER#{item_id}", "SK": f"SHARD#{shard}"},
        UpdateExpression="ADD #count :inc",
        ExpressionAttributeNames={"#count": "count"},
        ExpressionAttributeValues={":inc": 1}
    )

def get_counter(item_id, num_shards=10):
    response = table.query(
        KeyConditionExpression="PK = :pk",
        ExpressionAttributeValues={":pk": f"COUNTER#{item_id}"}
    )
    return sum(item["count"] for item in response["Items"])
```

### Sparse Indexes (GSI Filtering)
GSI only contains items that have the indexed attribute. Use this as a filter.

```python
# Only active users appear in GSI (saves cost)
# Set GSI key attribute only on active users
if user.is_active:
    item["GSI_ACTIVE_PK"] = "ACTIVE"
    item["GSI_ACTIVE_SK"] = user.last_login
# Inactive users don't have these attributes → not in GSI
```

### DynamoDB Streams + Lambda (CDC)
```
Table change → DynamoDB Stream → Lambda → 
  ├── Update Elasticsearch (search index)
  ├── Update cache (Redis)
  ├── Send notification (SNS)
  └── Replicate to another region
```

### TTL (Auto-Expiration)
```python
import time

table.put_item(Item={
    "PK": "SESSION#abc123",
    "SK": "DATA",
    "data": session_data,
    "ttl": int(time.time()) + 3600  # Expires in 1 hour
})
# DynamoDB deletes expired items within 48 hours (eventually)
# Free! No WCU cost for TTL deletions
```

## Pagination

DynamoDB returns max 1MB per query. Use `LastEvaluatedKey` for pagination:

```python
def query_all_items(pk):
    items = []
    kwargs = {
        "KeyConditionExpression": "PK = :pk",
        "ExpressionAttributeValues": {":pk": pk}
    }
    while True:
        response = table.query(**kwargs)
        items.extend(response["Items"])
        if "LastEvaluatedKey" not in response:
            break
        kwargs["ExclusiveStartKey"] = response["LastEvaluatedKey"]
    return items
```

## Anti-Patterns

| Anti-Pattern | Problem | Solution |
|-------------|---------|----------|
| Scan operations | Reads entire table (expensive, slow) | Design proper keys for Query |
| Large items (> 400KB) | 400KB item size limit | Store large data in S3, reference in DDB |
| Hot partition | One partition gets all traffic | Write sharding, better key design |
| Relational thinking | Trying to normalize data | Single-table design, denormalization |
| Too many GSIs | Each GSI doubles write cost | Max 5 GSIs, design carefully |

## DynamoDB vs Other Databases

| Feature | DynamoDB | MongoDB | PostgreSQL |
|---------|----------|---------|-----------|
| Scaling | Automatic, unlimited | Manual sharding | Vertical + read replicas |
| Latency | Single-digit ms at any scale | Single-digit ms | Depends on query |
| Schema | Schemaless | Schemaless | Schema required |
| Joins | ❌ | ❌ (but $lookup) | ✅ |
| Transactions | ✅ (25 items, 4MB) | ✅ | ✅ (full ACID) |
| Cost (small) | On-demand is cheap | Self-hosted or Atlas | RDS pricing |
| Cost (large) | Can be expensive | Moderate | Moderate |

## Interview Answer

> "DynamoDB's single-table design is key — you model your data around access patterns, not entities. Each item uses a composite primary key (PK + SK) with overloaded attributes, and GSIs provide alternative access patterns. For example, in an e-commerce app, user profiles, orders, and order items all live in the same table with carefully designed keys like 'USER#123' + 'ORDER#001'. The critical rules are: always know your access patterns upfront, use begins_with for hierarchical queries, use sparse GSIs for filtering, and implement write sharding for hot keys."
