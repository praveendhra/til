# Helm Charts

## What Is Helm?
Package manager for Kubernetes. Charts are reusable bundles of K8s manifests.

## Chart Structure
```
mychart/
├── Chart.yaml          # Metadata
├── values.yaml         # Default configuration
├── templates/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── _helpers.tpl    # Template helpers
│   └── NOTES.txt       # Post-install notes
└── charts/             # Dependencies
```

## Templating
```yaml
# templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "mychart.fullname" . }}
spec:
  replicas: {{ .Values.replicaCount }}
  template:
    spec:
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
```

## Common Commands
```bash
helm install myapp ./mychart -f production-values.yaml
helm upgrade myapp ./mychart --set image.tag=v2.0
helm rollback myapp 1
helm list -A
helm template mychart ./mychart  # Dry-run render
```

## values.yaml Best Practices
- Group related values
- Use descriptive comments
- Provide sensible defaults
- Document all values in README
