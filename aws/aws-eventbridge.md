# AWS EventBridge

## What Is It?
Serverless event bus for building event-driven architectures.

## Event Structure
```json
{
  "source": "com.myapp.orders",
  "detail-type": "OrderPlaced",
  "detail": {
    "orderId": "12345",
    "amount": 99.99,
    "customer": "user-678"
  }
}
```

## Event Rules (Pattern Matching)
```json
{
  "source": ["com.myapp.orders"],
  "detail-type": ["OrderPlaced"],
  "detail": {
    "amount": [{"numeric": [">", 100]}]
  }
}
```

## Targets
- Lambda functions
- SQS queues
- SNS topics
- Step Functions
- ECS tasks
- API Gateway
- EventBridge (cross-account/region)

## Schema Registry
- Auto-discovers event schemas
- Generates code bindings (Python, Java, TypeScript)
- Schema versioning

## Use Cases
- Decouple microservices
- Fan-out events to multiple consumers
- Cross-account event routing
- SaaS integration (Shopify, Zendesk, Auth0)
- Scheduled events (cron replacement)
