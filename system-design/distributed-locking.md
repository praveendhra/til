# Distributed Locking

## Problem
Multiple processes/services need exclusive access to a shared resource.

## Redis-Based Lock (Redlock)
```python
import redis
import uuid
import time

def acquire_lock(conn, lock_name, timeout=10):
    identifier = str(uuid.uuid4())
    end = time.time() + timeout
    while time.time() < end:
        if conn.set(f"lock:{lock_name}", identifier, nx=True, ex=timeout):
            return identifier
        time.sleep(0.001)
    return None

def release_lock(conn, lock_name, identifier):
    # Lua script for atomic check-and-delete
    script = '''
    if redis.call("get", KEYS[1]) == ARGV[1] then
        return redis.call("del", KEYS[1])
    end
    return 0
    '''
    conn.eval(script, 1, f"lock:{lock_name}", identifier)
```

## Redlock Algorithm (Multi-node)
1. Get current time
2. Try to acquire lock on N/2+1 Redis nodes
3. Calculate elapsed time
4. Lock acquired if majority agrees AND elapsed < TTL

## ZooKeeper-Based Lock
- Create ephemeral sequential znode
- Watch the previous znode
- Lock acquired when your znode is smallest

## DynamoDB Lock
- Conditional write with `attribute_not_exists`
- Heartbeat to extend lease
- Automatic expiration via TTL

## Key Considerations
- **Fencing tokens**: Monotonic token to prevent stale lock holders
- **Clock drift**: Can cause issues with time-based TTL
- **Network partitions**: Lock may be held by disconnected client
