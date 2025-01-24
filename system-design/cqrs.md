# CQRS - Command Query Responsibility Segregation

## Concept
Separate the read (query) and write (command) sides of an application into different models.

## Architecture
```
                ┌─── Command Model ───── Write DB (normalized)
User Request ───┤
                └─── Query Model ────── Read DB (denormalized)
```

## When to Use CQRS
- High read-to-write ratio
- Complex domain logic on the write side
- Need for different read optimizations
- Combined with Event Sourcing

## Implementation
```python
# Command side - validates and writes
class CreateOrderCommand:
    def execute(self, order_data):
        order = Order.create(order_data)  # Domain logic
        self.event_store.append(OrderCreated(order))
        return order.id

# Query side - optimized reads
class OrderQueryService:
    def get_order_summary(self, order_id):
        return self.read_db.query(
            "SELECT * FROM order_summaries WHERE id = ?", order_id
        )
```

## Sync Strategies
1. **Synchronous** – Update read model in same transaction
2. **Asynchronous** – Event-driven projection updates
3. **Eventual consistency** – Accept slight delay in reads
