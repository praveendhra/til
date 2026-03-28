# Feature Flags

## What Are They?
Runtime toggles that control feature visibility without code deployments.

## Types
| Type | Lifespan | Example |
|------|----------|---------|
| Release | Short | New checkout flow |
| Experiment | Medium | A/B test pricing |
| Ops | Variable | Kill switch for feature |
| Permission | Long | Premium features |

## Implementation
```python
# Simple feature flag check
if feature_flags.is_enabled("new_checkout", user_id=user.id):
    return new_checkout_flow(cart)
else:
    return legacy_checkout_flow(cart)
```

## Targeting Rules
```json
{
  "flag": "new_checkout",
  "rules": [
    {"attribute": "email", "operator": "endsWith", "value": "@company.com", "enabled": true},
    {"attribute": "country", "operator": "in", "value": ["US", "CA"], "percentage": 25},
    {"default": false}
  ]
}
```

## Best Practices
- Remove flags after rollout (tech debt!)
- Use a feature flag service (LaunchDarkly, Unleash, Flagsmith)
- Log flag evaluations for debugging
- Have a kill switch for every flag
- Test with flag on AND off

## Deployment Strategy
```
Deploy code (flag off) → Enable for internal → 5% canary → 25% → 100% → Remove flag
```

## Tools
| Tool | Type | Open Source |
|------|------|-----------|
| LaunchDarkly | SaaS | No |
| Unleash | Self-hosted | Yes |
| Flagsmith | Both | Yes |
| AWS AppConfig | Managed | N/A |
