# Infrastructure Testing

## Testing Pyramid for IaC
```
         /\
        /  \   End-to-End (deploy + validate)
       /----\
      / Integ \  Integration (plan + policy)
     /--------\
    /  Static   \ Static analysis (lint, validate)
   /--------------\
```

## Static Analysis
```bash
# Terraform
terraform validate
terraform fmt -check
tflint
tfsec              # Security scanning
checkov -d .       # Policy as code

# Kubernetes
kubeval manifest.yaml
kube-linter lint .
kubeconform -strict manifest.yaml
```

## Policy Testing (OPA/Conftest)
```rego
# policy/terraform.rego
package main

deny[msg] {
    resource := input.resource_changes[_]
    resource.type == "aws_s3_bucket"
    not resource.change.after.server_side_encryption_configuration
    msg := sprintf("S3 bucket '%s' must have encryption", [resource.address])
}
```

```bash
conftest test tfplan.json -p policy/
```

## Integration Testing (Terratest)
```go
func TestVPC(t *testing.T) {
    opts := &terraform.Options{
        TerraformDir: "../modules/vpc",
        Vars: map[string]interface{}{
            "environment": "test",
        },
    }
    defer terraform.Destroy(t, opts)
    terraform.InitAndApply(t, opts)

    vpcId := terraform.Output(t, opts, "vpc_id")
    assert.NotEmpty(t, vpcId)
}
```

## Infrastructure Drift Detection
- Terraform: `terraform plan` in CI (detect drift)
- AWS Config: Continuous compliance monitoring
- ArgoCD: Self-heal on Kubernetes drift
