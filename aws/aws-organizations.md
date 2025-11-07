# AWS Organizations

## What Is It?
Central management of multiple AWS accounts with consolidated billing and governance.

## Account Structure
```
Management Account (root)
├── Core OU
│   ├── Security Account (GuardDuty, SecurityHub)
│   ├── Log Archive Account (CloudTrail, Config)
│   └── Shared Services (Active Directory, CI/CD)
├── Production OU
│   ├── Prod App A Account
│   └── Prod App B Account
├── Non-Production OU
│   ├── Dev Account
│   └── Staging Account
└── Sandbox OU
    └── Developer Sandbox Accounts
```

## Service Control Policies (SCPs)
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Deny",
    "Action": [
      "ec2:RunInstances"
    ],
    "Resource": "*",
    "Condition": {
      "StringNotEquals": {
        "aws:RequestedRegion": ["us-east-1", "us-west-2"]
      }
    }
  }]
}
```

## Key Features
- Consolidated billing across all accounts
- Volume discounts applied automatically
- Cross-account access via IAM roles
- Service Control Policies for guardrails
- AWS Control Tower for automated setup

## Best Practices
- One workload per account
- Separate security/audit account
- Use SSO for human access
- Apply SCPs at OU level, not account level
