# Azure Policy

## What Is It?
Governance tool that enforces rules across Azure resources to maintain compliance.

## Policy Effects
| Effect | Behavior |
|--------|----------|
| Deny | Block non-compliant resource creation |
| Audit | Log non-compliance (don't block) |
| Append | Add fields to resource |
| Modify | Add/update/remove tags or properties |
| DeployIfNotExists | Deploy a related resource if missing |
| AuditIfNotExists | Audit if related resource missing |

## Example: Require Tags
```json
{
  "if": {
    "field": "tags['Environment']",
    "exists": "false"
  },
  "then": {
    "effect": "deny"
  }
}
```

## Example: Allowed Regions
```json
{
  "if": {
    "not": {
      "field": "location",
      "in": ["eastus", "westus2", "westeurope"]
    }
  },
  "then": {
    "effect": "deny"
  }
}
```

## Policy Initiatives (Blueprints)
Group related policies:
- CIS Benchmark
- NIST 800-53
- PCI DSS
- ISO 27001
