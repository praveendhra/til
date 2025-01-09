# Event Sourcing

## Core Idea
Instead of storing current state, store a sequence of events that led to the current state.

## Traditional vs Event Sourcing
```
Traditional: UPDATE accounts SET balance = 950 WHERE id = 1
Event Sourcing: INSERT INTO events (AccountDebited, {id: 1, amount: 50, timestamp: ...})
```

## Benefits
- Complete audit trail
- Temporal queries (state at any point in time)
- Event replay for debugging
- Natural fit for CQRS

## Event Store Schema
```sql
CREATE TABLE events (
    event_id     UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    event_data   JSONB NOT NULL,
    metadata     JSONB,
    version      INTEGER NOT NULL,
    created_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE(aggregate_id, version)
);
```

## Projections
Events are projected into read-optimized views:
- Rebuild projections by replaying events
- Multiple projections from the same event stream
- Eventually consistent read models

## Challenges
- Event schema evolution (upcasting)
- Snapshot optimization for long event streams
- Eventually consistent reads
- Increased storage requirements
