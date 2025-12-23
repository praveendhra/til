# Azure Identity - Managed Identities and RBAC

## Managed Identity
No credentials in code. Azure handles authentication automatically.

### System-Assigned
- Tied to a resource (App Service, VM, Function)
- Deleted when resource is deleted
- 1:1 relationship
```bash
az webapp identity assign --name myapp --resource-group myrg
```

### User-Assigned
- Independent resource, can be shared across resources
- Lifecycle managed separately
- Many:many relationship

## RBAC (Role-Based Access Control)
```
Who (Security Principal) + What (Role) + Where (Scope)
```

### Built-in Roles
- **Owner**: Full access + assign roles
- **Contributor**: Full access, cannot assign roles
- **Reader**: View only
- **Specific**: Storage Blob Data Reader, AKS Cluster Admin, etc.

### Scope Hierarchy
```
Management Group
  └── Subscription
        └── Resource Group
              └── Resource
```
Roles assigned at higher scope are inherited by children.

## Best Practices
1. Use **Managed Identities** instead of service principals with secrets
2. Use **Azure Key Vault** for any remaining secrets
3. Grant roles at the **narrowest scope** possible
4. Use **PIM** (Privileged Identity Management) for just-in-time elevation
5. Review access with **Access Reviews**

## Comparison
| Azure | AWS Equivalent |
|-------|---------------|
| Managed Identity | IAM Role for EC2/Lambda |
| RBAC | IAM Policies |
| Azure AD | IAM Users/Groups |
| Key Vault | Secrets Manager / KMS |
| PIM | IAM Access Analyzer |
