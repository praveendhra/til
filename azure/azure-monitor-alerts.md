# Azure Monitor Alerts

## Alert Types
1. **Metric alerts** – Based on numeric metrics (CPU, memory, etc.)
2. **Log alerts** – Based on Log Analytics queries (KQL)
3. **Activity log alerts** – Based on Azure resource events
4. **Smart detection** – Application Insights anomaly detection

## KQL Alert Example
```kql
// Alert when error rate exceeds 5% in last 5 minutes
requests
| where timestamp > ago(5m)
| summarize total = count(), errors = countif(resultCode >= 500)
| extend errorRate = todouble(errors) / todouble(total) * 100
| where errorRate > 5
```

## Action Groups
When alert fires, trigger:
- Email/SMS/Push notifications
- Azure Function
- Logic App
- Webhook
- ITSM connector (ServiceNow)
- Automation Runbook

## Best Practices
- Use severity levels (0-Critical to 4-Verbose)
- Set appropriate evaluation frequency
- Use suppression to avoid alert storms
- Create dashboards alongside alerts
- Use dynamic thresholds for seasonal patterns
