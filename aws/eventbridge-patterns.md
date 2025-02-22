# EventBridge Event Patterns and Rules

## What is EventBridge?
Serverless event bus. Route events from AWS services, SaaS apps, and custom apps.

## Event Structure
```json
{
  "source": "com.myapp.orders",
  "detail-type": "OrderPlaced",
  "detail": {
    "orderId": "12345",
    "amount": 99.99,
    "customer": "usr_789"
  }
}
```

## Event Patterns (Filter Rules)
```json
{
  "source": ["com.myapp.orders"],
  "detail-type": ["OrderPlaced"],
  "detail": {
    "amount": [{"numeric": [">", 100]}]
  }
}
```

## Common Targets
- Lambda (most common)
- SQS / SNS
- Step Functions
- API Gateway
- CloudWatch Logs
- Another EventBridge bus (cross-account)

## Use Cases
1. **Decouple microservices**: Order service publishes event, inventory/email/analytics services react
2. **Scheduled tasks**: Cron via EventBridge rules (replaced CloudWatch Events)
3. **AWS service events**: React to EC2 state changes, S3 uploads, CodePipeline status
4. **Cross-account routing**: Central event bus aggregates events from all accounts

## Schema Registry
Auto-discovers event schemas. Generate code bindings for type-safe event handling.
