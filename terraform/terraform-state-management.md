# Terraform State Management

## Remote State (Always Use This)
Never store state locally in team environments.

### AWS
```hcl
terraform {
  backend "s3" {
    bucket         = "my-terraform-state"
    key            = "prod/infrastructure.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}
```

### Azure
```hcl
terraform {
  backend "azurerm" {
    resource_group_name  = "tfstate-rg"
    storage_account_name = "tfstate12345"
    container_name       = "tfstate"
    key                  = "prod.tfstate"
  }
}
```

### GCP
```hcl
terraform {
  backend "gcs" {
    bucket = "my-terraform-state"
    prefix = "prod"
  }
}
```

## State Locking
Prevents concurrent modifications.
- S3 + DynamoDB (AWS)
- Blob Storage with lease (Azure)
- GCS with built-in locking (GCP)

## State File Security
- **Encrypt at rest** (SSE-S3, Azure Storage encryption)
- **Restrict access** (IAM policies for state bucket)
- **Never commit state to Git** (contains secrets!)
- **Enable versioning** on state bucket (rollback if corrupted)

## Workspaces
Isolate state for different environments:
```bash
terraform workspace new staging
terraform workspace new production
terraform workspace select production
terraform apply
```
