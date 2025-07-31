# GCP Cloud Build

## What Is It?
Serverless CI/CD platform that executes builds on Google Cloud.

## cloudbuild.yaml
```yaml
steps:
  # Run tests
  - name: 'python:3.12'
    entrypoint: 'bash'
    args:
      - '-c'
      - |
        pip install -r requirements.txt
        pytest tests/ -v

  # Build Docker image
  - name: 'gcr.io/cloud-builders/docker'
    args: ['build', '-t', 'gcr.io/$PROJECT_ID/myapp:$COMMIT_SHA', '.']

  # Push to Artifact Registry
  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', 'gcr.io/$PROJECT_ID/myapp:$COMMIT_SHA']

  # Deploy to Cloud Run
  - name: 'gcr.io/cloud-builders/gcloud'
    args:
      - 'run'
      - 'deploy'
      - 'myapp'
      - '--image=gcr.io/$PROJECT_ID/myapp:$COMMIT_SHA'
      - '--region=us-central1'
      - '--platform=managed'

images:
  - 'gcr.io/$PROJECT_ID/myapp:$COMMIT_SHA'

options:
  logging: CLOUD_LOGGING_ONLY
```

## Triggers
- Push to branch
- Tag creation
- Pull request
- Manual / API invocation
- Pub/Sub message

## vs GitHub Actions
| Feature | Cloud Build | GitHub Actions |
|---------|-----------|---------------|
| Hosting | GCP-native | GitHub-native |
| Pricing | Free tier: 120 min/day | Free tier: 2000 min/month |
| Integration | Deep GCP | Multi-cloud |
