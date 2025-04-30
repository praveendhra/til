# Back Pressure in Distributed Systems

## What Is Back Pressure?
A mechanism where a system resists accepting more work when it's at capacity, pushing back on the producer.

## Why It Matters
Without back pressure, systems fail catastrophically:
- OOM kills from unbounded queues
- Cascading failures across services
- Data loss from dropped messages

## Strategies

### 1. Blocking (Synchronous)
Producer blocks until consumer is ready.
```python
queue = Queue(maxsize=100)  # Blocks when full
queue.put(item)  # Blocks if queue is full
```

### 2. Buffering with Bounded Queues
Accept items up to a limit, then reject.
```python
if queue.qsize() < MAX_SIZE:
    queue.put(item)
else:
    return HTTPResponse(status=429)  # Too Many Requests
```

### 3. Dropping
Newest or oldest messages are dropped.
- **Tail drop**: Drop new arrivals
- **Head drop**: Drop oldest items
- Used in: video streaming, metrics

### 4. Flow Control (TCP-style)
Consumer advertises available capacity.
```
Consumer: "I can handle 100 more messages"
Producer: Sends up to 100, then waits
```

## Reactive Streams
```java
publisher.subscribe(new Subscriber<Data>() {
    public void onSubscribe(Subscription s) {
        s.request(10);  // Request 10 items (back pressure signal)
    }
    public void onNext(Data item) {
        process(item);
        subscription.request(1);  // Request 1 more
    }
});
```
