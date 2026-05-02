# AWS IAM — Least Privilege & Security Best Practices

## IAM Core Concepts

```
AWS Account
  └── IAM Users (people)
  └── IAM Roles (services, applications)
  └── IAM Groups (collections of users)
  └── IAM Policies (permission documents)
```

## Policy Evaluation Logic

```
                    ┌──────────────┐
                    │   Request    │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │ Explicit     │──── Yes ──→ DENY
                    │ Deny?        │
                    └──────┬───────┘
                           │ No
                    ┌──────▼───────┐
                    │ Explicit     │──── Yes ──→ ALLOW
                    │ Allow?       │
                    └──────┬───────┘
                           │ No
                    ┌──────▼───────┐
                    │  Default     │
                    │  DENY        │
                    └──────────────┘
```

**Rule**: Deny always wins. Explicit deny > explicit allow > implicit deny.

## Policy Types (Evaluated Together)

| Type | Attached To | Purpose | Example |
|------|-----------|---------|---------|
| **Identity-based** | User, Group, Role | Grant permissions to principal | "S3 read access" |
| **Resource-based** | S3 bucket, SQS queue | Grant cross-account access | S3 bucket policy |
| **Permission boundary** | User, Role | Maximum permissions ceiling | "Never allow IAM changes" |
| **SCP (Service Control Policy)** | AWS Organization OU | Account-level restrictions | "No resources outside us-east-1" |
| **Session policy** | STS session | Temporary restrictions | Federated user session |

### Effective Permissions

```
Identity-based ∩ Permission boundary ∩ SCP = Effective permissions

Example:
  Identity policy: Allow S3:*, EC2:*, IAM:*
  Permission boundary: Allow S3:*, EC2:*
  SCP: Allow S3:*, EC2:*, RDS:*
  
  Effective: S3:*, EC2:*  (intersection of all three)
```

## Least Privilege Patterns

### 1. Use Conditions for Fine-Grained Access
```json
{
  "Effect": "Allow",
  "Action": "s3:GetObject",
  "Resource": "arn:aws:s3:::my-bucket/*",
  "Condition": {
    "StringEquals": {
      "s3:prefix": ["home/${aws:username}/*"]
    },
    "IpAddress": {
      "aws:SourceIp": "10.0.0.0/8"
    },
    "Bool": {
      "aws:MultiFactorAuthPresent": "true"
    }
  }
}
```

### 2. Tag-Based Access Control (ABAC)
```json
{
  "Effect": "Allow",
  "Action": ["ec2:StartInstances", "ec2:StopInstances"],
  "Resource": "*",
  "Condition": {
    "StringEquals": {
      "ec2:ResourceTag/Environment": "${aws:PrincipalTag/Environment}"
    }
  }
}
```
Users can only manage EC2 instances tagged with their own environment.

### 3. IRSA (IAM Roles for Service Accounts) — EKS
```yaml
# Kubernetes ServiceAccount
apiVersion: v1
kind: ServiceAccount
metadata:
  name: my-app
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789:role/my-app-role

# Pod automatically gets temporary credentials for this role
# No long-lived AWS keys needed!
```

## Common Anti-Patterns

| Anti-Pattern | Problem | Fix |
|-------------|---------|-----|
| `"Action": "*"` | Full admin access | Specify exact actions needed |
| `"Resource": "*"` | Access to all resources | Scope to specific ARNs |
| Long-lived access keys | Compromise risk | Use roles, rotate keys (90 days max) |
| Shared IAM users | No accountability | One user per person, use SSO |
| Root account usage | Ultimate compromise risk | MFA, use only for billing/account tasks |
| Inline policies | Hard to audit/reuse | Use managed policies |

## IAM Access Analyzer

Automatically identifies resources shared externally and validates policies:

```bash
# Find resources shared outside your account
aws accessanalyzer list-findings --analyzer-arn <arn>

# Validate a policy before deploying
aws accessanalyzer validate-policy --policy-document file://policy.json --policy-type IDENTITY_POLICY
```

## Security Best Practices Checklist

- [ ] Enable MFA on root account and all IAM users
- [ ] Use IAM roles instead of access keys for applications
- [ ] Use IRSA for EKS workloads (pod-level IAM)
- [ ] Implement SCPs at the Organization level
- [ ] Set permission boundaries for delegated admin
- [ ] Enable CloudTrail for all regions
- [ ] Use IAM Access Analyzer to find overly permissive policies
- [ ] Rotate access keys every 90 days (or eliminate them)
- [ ] Use AWS SSO (IAM Identity Center) for human access
- [ ] Review unused credentials with IAM Credential Report

## Interview Answer

> "IAM follows the principle of least privilege — grant only the permissions needed, nothing more. I use IAM Roles everywhere instead of access keys — roles provide temporary credentials that auto-rotate. For EKS, IRSA gives pod-level IAM without sharing credentials. For multi-account setups, I use SCPs at the Organization level to enforce guardrails like 'no resources outside approved regions.' Permission boundaries set a ceiling that even admin users can't exceed. I always validate policies with IAM Access Analyzer before deploying."
