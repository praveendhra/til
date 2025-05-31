# GCP Compute Options

## Compute Engine (GCE)
Full VMs. IaaS.
- Custom machine types (choose exact vCPU/RAM)
- Preemptible/Spot VMs (up to 91% discount)
- Sole-tenant nodes for compliance
- **Use for**: Legacy apps, custom OS, GPU workloads

## Google Kubernetes Engine (GKE)
Managed Kubernetes. Most mature managed K8s.
- **Autopilot mode**: Google manages nodes, pay per pod
- **Standard mode**: You manage node pools
- GKE Gateway controller for L7 routing
- Config Sync + Policy Controller for GitOps
- **Use for**: Complex microservices, multi-cloud K8s

## Cloud Run
Serverless containers. Deploy any container, scale to zero.
- HTTP requests or Pub/Sub events
- Max 60 min timeout, 32GB memory
- Supports WebSockets, gRPC, streaming
- No K8s knowledge needed
- **Use for**: APIs, web apps, async processing

## Cloud Functions (2nd gen)
Serverless functions. Event-driven.
- Built on Cloud Run (2nd gen)
- Triggers: HTTP, Pub/Sub, Cloud Storage, Firestore, Eventarc
- Max 60 min timeout
- **Use for**: Lightweight event handling, webhooks, glue code

## Decision
```
Need full VM control? → Compute Engine
Need K8s + control? → GKE Standard
Want K8s without ops? → GKE Autopilot
Want containers without K8s? → Cloud Run
Want simple functions? → Cloud Functions
```
