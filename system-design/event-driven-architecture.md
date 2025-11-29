# Event-Driven Architecture Patterns

## Core Concepts
- **Event**: Something that happened (past tense) — `OrderPlaced`, `UserCreated`
- **Command**: Request to do something — `PlaceOrder`, `CreateUser`
- **Query**: Request for data — `GetOrder`, `ListUsers`

## CQRS (Command Query Responsibility Segregation)
Separate the write model from the read model:
```
Commands → Write DB (normalized, optimized for writes)
Queries  → Read DB (denormalized, optimized for reads)
```
Sync between them via events.

## Event Sourcing
Instead of storing current state, store ALL events:
```
UserCreated(id=1, name="Pravi")
EmailChanged(id=1, email="new@email.com")
AccountDeactivated(id=1)
```
Current state = replay all events. Full audit trail for free.

## Cloud Event Buses
| Service | Provider | Notes |
|---------|----------|-------|
| EventBridge | AWS | Serverless, schema registry, 90+ sources |
| Event Grid | Azure | Reactive programming, millions events/sec |
| Eventarc | GCP | Cloud Run/Functions triggers |
| Pub/Sub | GCP | Global messaging, exactly-once delivery |
| SNS + SQS | AWS | Fan-out pattern |
| Service Bus | Azure | Enterprise messaging with sessions/topics |

## Dead Letter Queues
Failed events go to DLQ for inspection. Always set up a DLQ — don't lose events silently.
