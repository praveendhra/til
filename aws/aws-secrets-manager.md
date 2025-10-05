# AWS Secrets Manager

## Key Features
- Automatic rotation of secrets (RDS, Redshift, DocumentDB)
- Cross-region replication
- Fine-grained IAM policies
- Audit trail via CloudTrail
- Automatic versioning

## Python SDK
```python
import boto3
import json

client = boto3.client("secretsmanager")

# Get secret
response = client.get_secret_value(SecretId="prod/db/credentials")
secret = json.loads(response["SecretString"])
db_password = secret["password"]
```

## Automatic Rotation
```python
# Lambda rotation function (simplified)
def lambda_handler(event, context):
    step = event["Step"]
    secret_id = event["SecretId"]

    if step == "createSecret":
        new_password = generate_password()
        client.put_secret_value(SecretId=secret_id, SecretString=new_password)
    elif step == "setSecret":
        update_database_password(new_password)
    elif step == "testSecret":
        test_database_connection(new_password)
    elif step == "finishSecret":
        client.update_secret_version_stage(SecretId=secret_id, ...)
```

## ECS Integration
```json
{
  "containerDefinitions": [{
    "secrets": [{
      "name": "DB_PASSWORD",
      "valueFrom": "arn:aws:secretsmanager:us-east-1:123456:secret:prod/db"
    }]
  }]
}
```

## Cost
- $0.40 per secret per month
- $0.05 per 10,000 API calls
