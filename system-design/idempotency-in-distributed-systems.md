# Idempotency in Distributed Systems

## The Problem
Network failures cause retries. Without idempotency:
```
Client → "Charge $50" → Server (processes)
Client → (timeout, retries) → "Charge $50" → Server (processes AGAIN)
Result: Customer charged $100 instead of $50
```

## The Solution
Make operations idempotent: executing N times = executing once.

## Implementation Patterns

### 1. Idempotency Key
Client generates unique key per operation:
```
POST /api/payments
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000

→ Server checks: "Have I seen this key before?"
→ Yes: Return cached response
→ No: Process and store response with key
```

### 2. Database Constraints
```sql
INSERT INTO payments (idempotency_key, amount, status)
VALUES ('550e8400...', 50.00, 'completed')
ON CONFLICT (idempotency_key) DO NOTHING;
```

### 3. Conditional Writes (Optimistic Concurrency)
```
PUT /api/resource/123
If-Match: "etag-v1"
```

## Naturally Idempotent Operations
- GET, PUT, DELETE (by spec)
- Setting a value: `SET balance = 100`

## NOT Naturally Idempotent
- POST (creating resources)
- Incrementing: `balance += 50`
- Appending to lists

## AWS: DynamoDB supports conditional writes. SQS has deduplication IDs.
## Azure: Service Bus has duplicate detection. Cosmos DB has conditional updates.
