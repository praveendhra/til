# Kubernetes RBAC

## Core Components
- **Role**: Set of permissions within a namespace
- **ClusterRole**: Set of permissions cluster-wide
- **RoleBinding**: Binds a Role to users/groups/service accounts
- **ClusterRoleBinding**: Binds a ClusterRole cluster-wide

## Example: Read-Only Role
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: production
  name: pod-reader
rules:
  - apiGroups: [""]
    resources: ["pods", "pods/log"]
    verbs: ["get", "list", "watch"]
  - apiGroups: ["apps"]
    resources: ["deployments"]
    verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  namespace: production
  name: read-pods
subjects:
  - kind: Group
    name: developers
    apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: Role
  name: pod-reader
  apiGroup: rbac.authorization.k8s.io
```

## Common ClusterRoles
| ClusterRole | Access |
|------------|--------|
| view | Read-only to most resources |
| edit | Read/write but no RBAC |
| admin | Full access within namespace |
| cluster-admin | Full cluster access |

## Best Practices
- Principle of least privilege
- Use Groups, not individual users
- Namespace-scoped roles when possible
- Audit with `kubectl auth can-i`
