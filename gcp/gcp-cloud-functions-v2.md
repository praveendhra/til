# GCP Cloud Functions (2nd Gen)

## What's New in 2nd Gen?
- Built on Cloud Run (more resources, longer timeout)
- Concurrency: handle multiple requests per instance
- Traffic splitting for gradual rollouts
- Eventarc integration for event sources
- Up to 60 min timeout (vs 9 min in 1st gen)

## HTTP Function
```python
import functions_framework

@functions_framework.http
def hello(request):
    name = request.args.get("name", "World")
    return f"Hello, {name}!"
```

## CloudEvent Function
```python
import functions_framework
from cloudevents.http import CloudEvent

@functions_framework.cloud_event
def process_storage(cloud_event: CloudEvent):
    data = cloud_event.data
    bucket = data["bucket"]
    file = data["name"]
    print(f"New file: gs://{bucket}/{file}")
```

## Deploy
```bash
gcloud functions deploy my-function \
  --gen2 \
  --runtime=python312 \
  --region=us-central1 \
  --trigger-http \
  --allow-unauthenticated \
  --memory=512Mi \
  --cpu=1 \
  --concurrency=80 \
  --min-instances=1
```

## Eventarc Triggers
- Cloud Storage events
- Pub/Sub messages
- Cloud Audit Logs (any GCP API call)
- Firebase events
- Custom events
