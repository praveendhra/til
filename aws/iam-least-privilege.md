# IAM Least Privilege Patterns

## Principle
Grant only the permissions needed, nothing more.

## Anti-patterns (DON'T DO THIS)
```json
{
  "Effect": "Allow",
  "Action": "*",
  "Resource": "*"
}
```

## Best Practices

### 1. Use IAM Access Analyzer
Finds resources shared externally and unused permissions.
```bash
aws accessanalyzer create-analyzer --analyzer-name my-analyzer --type ACCOUNT
```

### 2. Permission Boundaries
Cap maximum permissions for IAM entities:
```json
{
  "Effect": "Allow",
  "Action": ["s3:*", "dynamodb:*"],
  "Resource": "*"
}
```
Even if a role policy grants `ec2:*`, boundary blocks it.

### 3. Service Control Policies (SCPs)
Organization-wide guardrails:
```json
{
  "Effect": "Deny",
  "Action": ["organizations:LeaveOrganization"],
  "Resource": "*"
}
```

### 4. Use Roles, Not Long-Lived Keys
- EC2 → Instance Profile
- Lambda → Execution Role
- ECS → Task Role
- Cross-account → AssumeRole

### 5. Condition Keys
```json
{
  "Effect": "Allow",
  "Action": "s3:GetObject",
  "Resource": "arn:aws:s3:::my-bucket/*",
  "Condition": {
    "IpAddress": {"aws:SourceIp": "10.0.0.0/8"},
    "StringEquals": {"s3:ExistingObjectTag/env": "prod"}
  }
}
```
