# Terraform State Management — Deep Dive

## What Is Terraform State?

State is a JSON file (`terraform.tfstate`) that maps your Terraform configuration to real-world infrastructure.

```
main.tf declares:            terraform.tfstate maps:
  aws_instance "web" {}  →    i-0abc123def456 (real EC2 instance)
  aws_s3_bucket "data" {}  →  my-data-bucket (real S3 bucket)
```

## Why Remote State?

```
❌ Local state (terraform.tfstate on disk):
  - Not shared across team members
  - No locking → concurrent applies corrupt state
  - Lost laptop = lost state = orphaned infrastructure!

✅ Remote state (S3 + DynamoDB):
  - Shared across team
  - Locking prevents concurrent modifications
  - Versioned and backed up
```

## Setting Up Remote State (AWS)

### 1. Bootstrap the Backend (Chicken-and-Egg)

```hcl
# bootstrap/main.tf — Run this first with local state
resource "aws_s3_bucket" "terraform_state" {
  bucket = "my-company-terraform-state"

  lifecycle {
    prevent_destroy = true  # Never accidentally delete state!
  }
}

resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"  # Keep history of state changes
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "aws:kms"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket                  = aws_s3_bucket.terraform_state.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_dynamodb_table" "terraform_locks" {
  name         = "terraform-state-locks"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }
}
```

### 2. Configure Backend

```hcl
# backend.tf
terraform {
  backend "s3" {
    bucket         = "my-company-terraform-state"
    key            = "prod/networking/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-state-locks"
    encrypt        = true
  }
}
```

## State Locking

```
Without locking:
  Developer A: terraform apply (reads state, starts changes)
  Developer B: terraform apply (reads SAME state, starts changes)
  → Both overwrite each other → corrupted infrastructure!

With DynamoDB locking:
  Developer A: terraform apply → acquires lock ✅
  Developer B: terraform apply → "Error: state locked by Developer A" ❌
  Developer A: finishes → releases lock
  Developer B: retries → acquires lock ✅
```

## State File Structure

```
State directory structure (use key path):
  s3://terraform-state/
    ├── prod/
    │   ├── networking/terraform.tfstate
    │   ├── compute/terraform.tfstate
    │   ├── database/terraform.tfstate
    │   └── monitoring/terraform.tfstate
    ├── staging/
    │   ├── networking/terraform.tfstate
    │   └── compute/terraform.tfstate
    └── shared/
        └── iam/terraform.tfstate
```

## Workspaces vs Directory Structure

### Workspaces (Built-in, Simple)
```bash
terraform workspace new staging
terraform workspace new production
terraform workspace select production

# State stored at: env:/production/terraform.tfstate
```

**Pros**: Simple, built into Terraform
**Cons**: Same config for all environments (use variables for differences), easy to apply to wrong workspace

### Directory Structure (Recommended for Production)
```
infrastructure/
├── modules/           # Reusable modules
│   ├── vpc/
│   ├── eks/
│   └── rds/
├── environments/
│   ├── prod/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── backend.tf  # Points to prod state
│   ├── staging/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── backend.tf  # Points to staging state
│   └── dev/
└── global/            # Shared resources (IAM, DNS)
    ├── main.tf
    └── backend.tf
```

## State Operations (Emergency Commands)

```bash
# List all resources in state
terraform state list

# Show details of a specific resource
terraform state show aws_instance.web

# Move a resource (rename without destroy/recreate)
terraform state mv aws_instance.old aws_instance.new

# Remove from state (resource continues to exist, Terraform forgets it)
terraform state rm aws_instance.imported

# Import existing resource into state
terraform import aws_instance.web i-0abc123def456

# Force unlock (if lock is stuck — use with caution!)
terraform force-unlock LOCK_ID
```

## Handling State Drift

```
State drift: Real infrastructure differs from state file

Detection:
  terraform plan → shows unexpected changes

Causes:
  - Manual changes in console (ClickOps)
  - Another tool modified the resource
  - Auto-scaling added/removed instances

Resolution:
  1. terraform refresh → update state to match reality
  2. Review the plan
  3. Either accept the drift or terraform apply to revert
```

## Best Practices

1. **Always use remote state** with locking
2. **Never edit state manually** (use `terraform state` commands)
3. **Enable versioning** on the S3 bucket (rollback if state gets corrupted)
4. **Encrypt state** (contains sensitive data: passwords, keys, IPs)
5. **Separate state per environment** (prod changes can't affect staging)
6. **Separate state per component** (networking, compute, data — limit blast radius)
7. **Use `prevent_destroy`** on critical resources (state bucket, databases)

## Interview Answer

> "I store Terraform state in S3 with DynamoDB locking and KMS encryption. State is separated by environment and component — so prod/networking, prod/compute, and staging/networking each have their own state file. This limits the blast radius: a bad apply to networking can't affect compute resources. I use a directory structure per environment (not workspaces) for clear separation. The state bucket has versioning enabled so we can recover from corruption. In CI/CD, only the pipeline runs terraform apply — engineers run plan locally but apply goes through the pipeline with approval gates."
