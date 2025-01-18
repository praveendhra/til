# Azure Key Vault

## What It Stores
1. **Secrets**: Connection strings, API keys, passwords
2. **Keys**: Cryptographic keys (RSA, EC) for encryption/signing
3. **Certificates**: SSL/TLS certificates with auto-renewal

## Access from Code (Managed Identity)
```python
from azure.identity import DefaultAzureCredential
from azure.keyvault.secrets import SecretClient

credential = DefaultAzureCredential()
client = SecretClient(vault_url="https://myvault.vault.azure.net", credential=credential)
secret = client.get_secret("db-password")
print(secret.value)
```

No credentials in code! Managed Identity authenticates automatically.

## Key Vault Reference in App Service
```json
// In App Service Configuration
"ConnectionString": "@Microsoft.KeyVault(SecretUri=https://myvault.vault.azure.net/secrets/db-conn)"
```
App Service resolves the reference at runtime. Zero code changes.

## Best Practices
1. **Separate vaults** per environment (dev, staging, prod)
2. **Soft-delete** enabled (90-day recovery window)
3. **Purge protection** for compliance
4. **Access policies** or RBAC (prefer RBAC for granularity)
5. **Secret rotation** with Event Grid notifications
6. **Network rules** — restrict to VNet via private endpoint

## Comparison
| Azure Key Vault | AWS Secrets Manager | GCP Secret Manager |
|----------------|--------------------|--------------------|
| Secrets + Keys + Certs | Secrets only (KMS for keys) | Secrets only (Cloud KMS) |
| HSM-backed | HSM via CloudHSM | HSM via Cloud HSM |
| ARM integration | CloudFormation | Terraform |
