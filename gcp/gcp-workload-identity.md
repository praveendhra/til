# GCP Workload Identity Federation

## Problem
Need to authenticate from external systems (GitHub Actions, AWS, Azure) to GCP without service account keys.

## How It Works
```
External Identity Provider → Workload Identity Pool → Service Account → GCP Resources
```

1. External system gets a token from its identity provider
2. Token is exchanged for a GCP short-lived credential
3. Credential impersonates a GCP service account

## GitHub Actions Setup
```yaml
# In GitHub Actions workflow
- uses: google-github-actions/auth@v2
  with:
    workload_identity_provider: 'projects/123/locations/global/workloadIdentityPools/github-pool/providers/github-provider'
    service_account: 'github-deployer@my-project.iam.gserviceaccount.com'
```

## Terraform Setup
```hcl
resource "google_iam_workload_identity_pool" "github" {
  workload_identity_pool_id = "github-pool"
}

resource "google_iam_workload_identity_pool_provider" "github" {
  workload_identity_pool_id          = google_iam_workload_identity_pool.github.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-provider"

  attribute_mapping = {
    "google.subject"       = "assertion.sub"
    "attribute.repository" = "assertion.repository"
  }

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}
```

## Benefits
- No long-lived credentials to manage
- Automatic token rotation
- Fine-grained access control via attribute conditions
- Audit trail in Cloud Audit Logs
