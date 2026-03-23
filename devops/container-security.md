# Container Security Best Practices

## Image Security
1. Use minimal base images (distroless, alpine)
2. Pin image versions (never use `latest`)
3. Scan for vulnerabilities (Trivy, Snyk, Grype)
4. Multi-stage builds to reduce attack surface
5. Don't include secrets in images

## Dockerfile Hardening
```dockerfile
# Use specific version
FROM python:3.12-slim@sha256:abc123...

# Run as non-root
RUN groupadd -r app && useradd -r -g app app
USER app

# Read-only filesystem
# Set in K8s: readOnlyRootFilesystem: true

# No new privileges
# Set in K8s: allowPrivilegeEscalation: false
```

## Kubernetes Security Context
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
  capabilities:
    drop: ["ALL"]
  seccompProfile:
    type: RuntimeDefault
```

## Supply Chain Security
- **Image signing**: cosign (Sigstore)
- **SBOM**: Generate software bill of materials
- **Binary Authorization**: Only deploy verified images
- **Dependency scanning**: Automated CVE alerts

## Runtime Security
- **Falco**: Detect anomalous container behavior
- **Network policies**: Zero-trust networking
- **Pod Security Standards**: Enforce security baselines
- **Audit logging**: Track all API server requests
