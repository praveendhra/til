# Azure Monitor and Application Insights

## Azure Monitor Platform
Central observability platform for Azure.

### Data Types
- **Metrics**: Numeric time-series (CPU, memory, request count)
- **Logs**: Structured logs in Log Analytics workspace (KQL queries)
- **Traces**: Distributed traces via Application Insights
- **Alerts**: Metric alerts, log alerts, activity log alerts

## Application Insights
Full APM solution for web applications.

### Auto-Instrumentation
```python
# Python - just add the SDK
from opencensus.ext.azure.trace_exporter import AzureExporter
from opencensus.trace.tracer import Tracer

tracer = Tracer(exporter=AzureExporter(connection_string="..."))
```

### Key Features
- **Live Metrics**: Real-time performance view
- **Application Map**: Visual dependency map
- **Smart Detection**: AI-driven anomaly alerts
- **Availability Tests**: URL ping from global locations
- **Profiler**: Code-level perf analysis

## KQL (Kusto Query Language)
```kusto
requests
| where timestamp > ago(1h)
| where resultCode >= 500
| summarize count() by bin(timestamp, 5m), operation_Name
| render timechart
```

## Comparison
| Azure | AWS | GCP |
|-------|-----|-----|
| Azure Monitor | CloudWatch | Cloud Monitoring |
| App Insights | X-Ray + CloudWatch | Cloud Trace + Logging |
| Log Analytics | CloudWatch Logs Insights | Cloud Logging |
| KQL | CloudWatch Insights QL | MQL |
