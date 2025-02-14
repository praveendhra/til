# Infrastructure as Code Tools Compared

## Terraform (HashiCorp)
- **Language**: HCL (declarative)
- **Multi-cloud**: AWS, Azure, GCP, and 3000+ providers
- **State**: External state file (S3, GCS, Azure Blob)
- **Best for**: Multi-cloud, team standardization

## Pulumi
- **Language**: Python, TypeScript, Go, C#, Java
- **Multi-cloud**: Same providers as Terraform
- **State**: Pulumi Cloud or self-managed
- **Best for**: Teams that prefer real programming languages

## AWS CloudFormation
- **Language**: JSON/YAML (declarative)
- **Cloud**: AWS only
- **State**: Managed by AWS (no state file concerns)
- **Best for**: AWS-only shops, tight AWS integration
- **CDK**: Write CloudFormation in Python/TypeScript

## Azure Bicep
- **Language**: Bicep (declarative, compiles to ARM)
- **Cloud**: Azure only
- **State**: Managed by Azure
- **Best for**: Azure-only shops
```bicep
resource storageAccount 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'mystorageaccount'
  location: 'eastus'
  kind: 'StorageV2'
  sku: { name: 'Standard_LRS' }
}
```

## GCP Deployment Manager / Config Connector
- Deployment Manager: YAML-based (legacy)
- Config Connector: K8s-native GCP resource management

## My Recommendation
- Multi-cloud or multi-team → **Terraform**
- Azure-only → **Bicep** (simpler than Terraform for Azure)
- AWS-only + love Python → **CDK**
- Hate YAML → **Pulumi**
