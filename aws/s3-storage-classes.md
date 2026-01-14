# S3 Storage Classes and Lifecycle Policies

## Storage Classes (by cost, low → high access)
| Class | Use Case | Min Duration | Retrieval |
|-------|----------|-------------|-----------|
| S3 Glacier Deep Archive | Compliance archives | 180 days | 12-48 hrs |
| S3 Glacier Flexible | Backup/archive | 90 days | 1-12 hrs |
| S3 Glacier Instant | Archive w/ instant access | 90 days | Milliseconds |
| S3 Infrequent Access | Backups, DR | 30 days | Milliseconds |
| S3 One Zone-IA | Reproducible data | 30 days | Milliseconds |
| S3 Standard | Frequent access | None | Milliseconds |
| S3 Intelligent-Tiering | Unknown access pattern | None | Milliseconds |

## Lifecycle Policy Example
```json
{
  "Rules": [{
    "ID": "archive-old-logs",
    "Status": "Enabled",
    "Transitions": [
      {"Days": 30, "StorageClass": "STANDARD_IA"},
      {"Days": 90, "StorageClass": "GLACIER"},
      {"Days": 365, "StorageClass": "DEEP_ARCHIVE"}
    ],
    "Expiration": {"Days": 2555}
  }]
}
```

## Cost Optimization Tips
- Enable **S3 Intelligent-Tiering** for unknown access patterns
- Use **S3 Analytics** to find objects that can be transitioned
- **Multipart upload** for files > 100MB
- Enable **S3 Inventory** to audit what you're storing
