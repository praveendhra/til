# GCP Artifact Registry

## What Is It?
Universal package manager for container images, language packages, and OS packages.

## Supported Formats
- Docker images
- Maven / Gradle (Java)
- npm (Node.js)
- Python (pip)
- Go modules
- Helm charts
- Apt / Yum (OS packages)

## Docker Repository
```bash
# Create repository
gcloud artifacts repositories create my-repo \
  --repository-format=docker \
  --location=us-central1

# Configure Docker auth
gcloud auth configure-docker us-central1-docker.pkg.dev

# Tag and push
docker tag myapp us-central1-docker.pkg.dev/my-project/my-repo/myapp:v1
docker push us-central1-docker.pkg.dev/my-project/my-repo/myapp:v1
```

## Cleanup Policies
```bash
gcloud artifacts repositories set-cleanup-policies my-repo \
  --location=us-central1 \
  --policy=delete-old-versions \
  --keep-count=5
```

## Vulnerability Scanning
- Automatic scanning on push
- Integration with Binary Authorization
- View vulnerabilities in console or API

## vs Container Registry (gcr.io)
| Feature | Artifact Registry | Container Registry |
|---------|------------------|-------------------|
| Multi-format | Yes | Docker only |
| Regional | Yes | Multi-regional |
| IAM | Fine-grained | Bucket-level |
| Status | Recommended | Legacy |
