# Terraform Modules Best Practices

## Module Structure
```
modules/
├── vpc/
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── README.md
├── compute/
└── database/
```

## Writing Good Modules

### 1. Single Responsibility
One module = one logical resource group. Don't create a "kitchen sink" module.

### 2. Expose Sensible Defaults
```hcl
variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.medium"  # Good default
}
```

### 3. Output Everything Downstream Needs
```hcl
output "vpc_id" {
  description = "The ID of the VPC"
  value       = aws_vpc.main.id
}
```

### 4. Version Pin Providers
```hcl
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}
```

### 5. Use `for_each` Over `count`
```hcl
# BAD: count - index-based, fragile
resource "aws_subnet" "main" {
  count = 3
}

# GOOD: for_each - key-based, stable
resource "aws_subnet" "main" {
  for_each   = toset(["web", "app", "db"])
  cidr_block = var.subnet_cidrs[each.key]
  tags       = { Name = each.key }
}
```

## Registry Modules
Use verified modules from the Terraform Registry when possible:
```hcl
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"
  # ...
}
```
