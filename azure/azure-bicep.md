# Azure Bicep

## What Is It?
Domain-specific language (DSL) for deploying Azure resources. Compiles to ARM templates.

## Bicep vs ARM Template
```bicep
// Bicep (concise)
resource storageAccount 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'mystorageaccount'
  location: resourceGroup().location
  sku: { name: 'Standard_LRS' }
  kind: 'StorageV2'
}

output storageId string = storageAccount.id
```

vs ARM JSON (50+ lines for the same thing)

## Modules
```bicep
// main.bicep
module network './modules/network.bicep' = {
  name: 'networkDeploy'
  params: {
    vnetName: 'myVnet'
    location: location
  }
}

module app './modules/appService.bicep' = {
  name: 'appDeploy'
  params: {
    subnetId: network.outputs.subnetId
  }
}
```

## Key Features
- Type safety and IntelliSense in VS Code
- Modules for reusable components
- Conditional deployments (`if`)
- Loops (`for`)
- String interpolation
- No state file needed (unlike Terraform)

## Commands
```bash
az bicep build -f main.bicep        # Compile to ARM
az deployment group create \
  -g myRG -f main.bicep \
  -p environment=prod
```
