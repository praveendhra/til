# Incident Management — From Alert to Post-Mortem

## Incident Lifecycle

```
Detection → Triage → Response → Mitigation → Resolution → Post-Mortem
```

## Severity Levels

| Level | Definition | Response | Example |
|-------|-----------|----------|---------|
| **SEV-1** | Complete outage, data loss, security breach | Page on-call, start bridge immediately | Payment system down, data breach |
| **SEV-2** | Major feature degraded, significant user impact | Page on-call, respond within 15 min | Login failing for 30% of users |
| **SEV-3** | Minor feature affected, workaround available | Respond within 1 hour | Search slightly slow, non-critical API errors |
| **SEV-4** | Cosmetic/minor, no user impact | Next business day | Dashboard styling broken |

## Incident Response Framework

### 1. Detect
```
Automated alerting (PagerDuty/OpsGenie):
  - Error rate > 1% for 5 min → Page
  - P99 latency > 2s for 10 min → Page
  - Error budget burn rate > 14x → Page
  - Health check failing → Page

Manual detection:
  - Customer reports (support tickets, social media)
  - Internal reports (team members notice issues)
```

### 2. Triage (First 5 Minutes)
```
1. Acknowledge the alert
2. Assess severity (SEV-1 through SEV-4)
3. Check: Is it a real incident or a false alarm?
4. Quick checks:
   - Recent deployments? (git log, ArgoCD)
   - Recent config changes? (Terraform, feature flags)
   - External dependency outage? (status pages)
   - Infrastructure change? (cloud provider status)
```

### 3. Response Roles (For SEV-1/SEV-2)

| Role | Responsibility |
|------|---------------|
| **Incident Commander (IC)** | Coordinates response, makes decisions, communicates status |
| **Technical Lead** | Diagnoses and fixes the issue |
| **Communications Lead** | Updates stakeholders, status page, customers |
| **Scribe** | Documents timeline, actions taken, decisions |

### 4. Mitigation (Stop the Bleeding)

```
Common quick mitigations:
  1. Rollback deployment: kubectl rollout undo deployment/app
  2. Toggle feature flag: disable the new feature
  3. Scale up: kubectl scale deployment/app --replicas=20
  4. Redirect traffic: Route away from affected region
  5. Restart pods: kubectl delete pod -l app=my-app
  6. Revert config change: git revert + apply
  7. Block bad traffic: WAF rule, rate limiting

IMPORTANT: Mitigate first, diagnose later!
"Stop the bleeding before performing surgery"
```

### 5. Resolution
```
After mitigation stabilizes the system:
  1. Confirm metrics are back to normal
  2. Verify customer impact has ended  
  3. Close incident channel
  4. Schedule post-mortem (within 48 hours for SEV-1/2)
```

## Post-Mortem (Blameless)

### Template

```markdown
## Incident Post-Mortem: [Title]

**Date**: 2025-03-15
**Severity**: SEV-2
**Duration**: 47 minutes
**Impact**: 15% of users experienced payment failures
**Authors**: [Names]

### Summary
One-paragraph description of what happened.

### Timeline (All times UTC)
- 14:02 - Deploy v2.3.1 to production
- 14:15 - Error rate alert fires (5xx > 2%)
- 14:17 - On-call engineer acknowledges
- 14:22 - Identified: new payment validation rejects valid cards
- 14:25 - Decision: rollback deployment
- 14:28 - Rollback initiated
- 14:35 - Rollback complete, error rate normalizing
- 14:49 - Confirmed: all metrics normal, incident resolved

### Root Cause
The payment validation regex was updated to be stricter,
but it incorrectly rejected card numbers starting with '4'
(Visa cards) when the card had 19 digits (new format).

### Contributing Factors
- Test suite didn't cover 19-digit card numbers
- No canary deployment (went straight to 100%)
- Payment service lacked integration test with real card formats

### What Went Well
- Alert fired within 13 minutes
- Rollback was smooth and quick
- Team coordinated effectively in incident channel

### What Didn't Go Well
- No canary deployment for payment-critical changes
- 13 minutes to detect (should be faster for payment flow)
- No automated rollback on error rate spike

### Action Items
| # | Action | Owner | Priority | Due |
|---|--------|-------|----------|-----|
| 1 | Add 19-digit card test cases | @dev-lead | P0 | 2025-03-17 |
| 2 | Implement canary deploys for payment service | @platform | P1 | 2025-03-31 |
| 3 | Add synthetic monitoring for payment flow | @sre | P1 | 2025-03-24 |
| 4 | Set up auto-rollback on 5xx spike > 5% | @sre | P2 | 2025-04-15 |
```

### Blameless Culture

```
❌ "John pushed broken code without testing"
✅ "The testing process didn't catch the edge case because 
    integration tests don't cover 19-digit card numbers"

Focus on:
  - What process failed (not who)
  - What systems/tools should have caught this
  - What changes prevent recurrence
```

## On-Call Best Practices

- **Rotation**: Weekly, with handoff documentation
- **Escalation**: If not acknowledged in 5 min → escalate
- **Runbooks**: Pre-written diagnostic steps for common alerts
- **Compensation**: On-call pay or comp time
- **Toil budget**: Max 50% of on-call time on toil (Google SRE)

## Interview Answer

> "I follow a structured incident management process: detect through automated alerting (PagerDuty), triage severity within 5 minutes, and for SEV-1/2 incidents, assign an incident commander to coordinate response. The priority is always mitigation first — rollback, feature flag toggle, or scaling up — before root cause diagnosis. After resolution, we conduct blameless post-mortems within 48 hours, focusing on contributing factors and action items rather than blame. The post-mortem includes a detailed timeline, root cause analysis, and concrete action items with owners and deadlines."
