# GitOps with ArgoCD

## GitOps Principles
1. **Declarative**: System described entirely in Git
2. **Versioned**: Git as single source of truth
3. **Automated**: Approved changes auto-applied
4. **Self-healing**: Drift detected and corrected

## ArgoCD Application
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: myapp
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/myorg/k8s-manifests.git
    targetRevision: main
    path: apps/myapp/overlays/production
  destination:
    server: https://kubernetes.default.svc
    namespace: myapp
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
```

## Sync Strategies
- **Auto-sync**: Apply changes when Git changes
- **Manual sync**: Require human approval
- **Self-heal**: Revert manual kubectl changes
- **Prune**: Delete resources removed from Git

## App of Apps Pattern
```yaml
# Root application that manages other applications
spec:
  source:
    path: apps/  # Contains Application manifests
```

## vs Flux
| Feature | ArgoCD | Flux |
|---------|--------|------|
| UI | Built-in web UI | No UI (Weave GitOps) |
| Multi-cluster | Yes | Yes |
| Helm support | Yes | Yes |
| Kustomize | Yes | Yes |
| RBAC | Built-in | Kubernetes native |
