# Azure Storage Options

## Blob Storage
Object storage (like AWS S3).

| Tier | Use Case | Access Latency | Cost |
|------|----------|---------------|------|
| Hot | Frequent access | ms | $$$ |
| Cool | Infrequent (30+ days) | ms | $$ |
| Cold | Rare (90+ days) | ms | $ |
| Archive | Compliance (180+ days) | hours | ¢ |

### Lifecycle Management
```json
{
  "rules": [{
    "name": "archive-old",
    "definition": {
      "actions": {
        "baseBlob": {
          "tierToCool": {"daysAfterModificationGreaterThan": 30},
          "tierToArchive": {"daysAfterModificationGreaterThan": 90}
        }
      }
    }
  }]
}
```

## Table Storage
NoSQL key-value store. Simple, cheap. For basic scenarios, use **Cosmos DB Table API** for more features.

## Queue Storage
Simple message queue. For enterprise features, use **Service Bus**.

## Azure Files
Managed SMB/NFS file shares. Mount from VMs, containers, on-prem.
Replace on-prem file servers. Azure File Sync for hybrid.

## Data Lake Storage Gen2
Hierarchical namespace on top of Blob Storage. For big data analytics.
Works with Spark, Databricks, Synapse Analytics.
POSIX ACLs for fine-grained access.
