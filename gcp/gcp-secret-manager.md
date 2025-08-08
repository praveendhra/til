# GCP Secret Manager

## What Is It?
Managed service for storing API keys, passwords, certificates, and other sensitive data.

## Key Features
- Automatic replication (regional or user-managed)
- Secret versioning
- IAM-based access control
- Audit logging via Cloud Audit Logs
- Automatic rotation support

## CLI Usage
```bash
# Create a secret
echo -n "super-secret-value" | gcloud secrets create db-password --data-file=-

# Access a secret
gcloud secrets versions access latest --secret=db-password

# Add a new version
echo -n "new-password" | gcloud secrets versions add db-password --data-file=-
```

## Python SDK
```python
from google.cloud import secretmanager

client = secretmanager.SecretManagerServiceClient()
name = f"projects/my-project/secrets/db-password/versions/latest"

response = client.access_secret_version(request={"name": name})
secret_value = response.payload.data.decode("UTF-8")
```

## Integration with GKE
```yaml
apiVersion: v1
kind: Pod
metadata:
  annotations:
    secrets.hashicorp.com/agent-inject: "true"
spec:
  containers:
    - name: app
      env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
```

## Best Practices
- Use IAM conditions for time-limited access
- Enable audit logging
- Rotate secrets regularly
- Use workload identity for GKE access
