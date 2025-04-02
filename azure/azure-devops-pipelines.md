# Azure DevOps Pipelines - YAML Best Practices

## Pipeline Structure
```yaml
trigger:
  branches:
    include: [main, develop]
  paths:
    exclude: ['docs/*', '*.md']

pool:
  vmImage: 'ubuntu-latest'

variables:
  - group: production-vars
  - name: buildConfiguration
    value: 'Release'

stages:
  - stage: Build
    jobs:
      - job: BuildApp
        steps:
          - task: UseDotNet@2
            inputs:
              version: '8.x'
          - script: dotnet build --configuration $(buildConfiguration)
          - task: PublishBuildArtifacts@1

  - stage: Deploy
    dependsOn: Build
    condition: eq(variables['Build.SourceBranch'], 'refs/heads/main')
    jobs:
      - deployment: DeployProd
        environment: 'production'
        strategy:
          runOnce:
            deploy:
              steps:
                - script: echo "Deploying..."
```

## Best Practices
1. **Use templates** for reusable pipeline logic
2. **Variable groups** for secrets (linked to Key Vault)
3. **Environments** with approval gates for prod
4. **Caching** dependencies: `Cache@2` task
5. **Matrix strategy** for multi-platform builds
6. **Deployment strategy**: `canary`, `rolling`, or `blueGreen`

## Comparison with GitHub Actions
| Feature | Azure DevOps | GitHub Actions |
|---------|-------------|----------------|
| YAML pipelines | Yes | Yes |
| Environments | Yes (with approvals) | Yes (with approvals) |
| Self-hosted runners | Yes | Yes |
| Artifact management | Azure Artifacts | GitHub Packages |
| Board integration | Azure Boards | GitHub Projects |
