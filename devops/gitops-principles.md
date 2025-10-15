# GitOps Principles

## Core Idea
Git repo = desired state of infrastructure/applications. Changes via PR. Automated reconciliation.

## Four Principles (OpenGitOps)
1. **Declarative**: System state described declaratively (YAML, HCL)
2. **Versioned**: Desired state stored in Git (auditable history)
3. **Automated**: Approved changes auto-applied to system
4. **Reconciled**: Agent continuously ensures actual = desired state

## Tools
| Tool | Type | Notes |
|------|------|-------|
| ArgoCD | Pull-based | K8s-native, UI dashboard, SSO |
| Flux | Pull-based | K8s-native, CNCF graduated |
| Jenkins | Push-based | Traditional CI/CD |
| GitHub Actions | Push-based | Event-driven workflows |

## Pull vs Push
- **Push**: CI pipeline pushes changes to cluster (`kubectl apply`)
- **Pull**: Agent in cluster pulls desired state from Git (ArgoCD/Flux)

Pull is preferred — cluster credentials stay in cluster, not in CI.

## Workflow
```
Developer → PR → Review → Merge to main
                              ↓
                    ArgoCD detects change
                              ↓
                    Syncs cluster to desired state
                              ↓
                    Drift detected? Auto-correct
```
