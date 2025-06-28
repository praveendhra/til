# Azure Managed Identity

## Problem
Storing credentials (connection strings, API keys) in code or config is a security risk.

## Solution
Managed Identity provides an automatically managed identity in Azure AD for Azure resources.

## Types
### System-Assigned
- Tied to a single Azure resource
- Deleted when the resource is deleted
- One-to-one relationship

### User-Assigned
- Standalone Azure resource
- Can be shared across multiple resources
- Independent lifecycle

## Usage Pattern
```python
from azure.identity import DefaultAzureCredential
from azure.keyvault.secrets import SecretClient

# No passwords, keys, or connection strings!
credential = DefaultAzureCredential()
client = SecretClient(
    vault_url="https://myvault.vault.azure.net",
    credential=credential
)
secret = client.get_secret("database-password")
```

## DefaultAzureCredential Chain
1. Environment variables
2. Managed Identity
3. Azure CLI
4. Azure PowerShell
5. Interactive browser

## Common Use Cases
- App Service → Key Vault (retrieve secrets)
- App Service → Storage Account (read/write blobs)
- Functions → Service Bus (send/receive messages)
- AKS → ACR (pull container images)
