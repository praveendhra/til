# GCP IAM - Service Accounts and Workload Identity

## IAM Hierarchy
```
Organization
  └── Folder
        └── Project
              └── Resource
```
Permissions inherit downward. Policy at org level applies everywhere.

## Roles
- **Basic roles**: Owner, Editor, Viewer (too broad for production)
- **Predefined roles**: `roles/storage.objectViewer`, `roles/cloudsql.admin`
- **Custom roles**: Define exact permissions

## Service Accounts
Identity for applications and VMs (like AWS IAM Roles).

### Best Practices
1. **One service account per service** (not shared)
2. **No exported keys** — use Workload Identity instead
3. **Least privilege** — predefined roles > basic roles
4. **Short-lived credentials** — use impersonation

### Workload Identity (GKE)
Map K8s service account to GCP service account. No keys needed.
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: my-app
  annotations:
    iam.gke.io/gcp-service-account: my-app@project.iam.gserviceaccount.com
```

### Workload Identity Federation (External)
Access GCP from AWS, Azure, or any OIDC provider without service account keys.
```
GitHub Actions → OIDC token → GCP WIF → temporary GCP credentials
```

## Comparison
| GCP | AWS | Azure |
|-----|-----|-------|
| Service Account | IAM Role | Managed Identity |
| WIF | IAM OIDC Provider | Federated Credentials |
| IAM Conditions | IAM Conditions | RBAC Conditions |
| Organization Policy | SCP | Azure Policy |
