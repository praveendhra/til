# ECS vs EKS vs Lambda

## AWS Lambda (Serverless Functions)
- **Best for**: Event-driven, short-lived tasks (<15 min)
- **Scaling**: Automatic, 0 to thousands in seconds
- **Cost**: Pay per invocation + duration
- **Ops**: Zero infrastructure management
- **Limits**: 10GB memory, 15 min timeout, 10GB package

## ECS (Elastic Container Service)
- **Best for**: Containerized apps, AWS-native teams
- **Scaling**: Auto-scaling groups or Fargate (serverless containers)
- **Cost**: EC2 instances or Fargate per-vCPU/memory/hour
- **Ops**: Moderate — task definitions, services, load balancers
- **ECS Fargate**: No EC2 management, just define CPU/memory

## EKS (Elastic Kubernetes Service)
- **Best for**: Multi-cloud strategy, existing K8s expertise, complex orchestration
- **Scaling**: Cluster Autoscaler + HPA
- **Cost**: $0.10/hr control plane + EC2/Fargate nodes
- **Ops**: Higher — kubectl, Helm, RBAC, networking
- **EKS Fargate**: Serverless K8s pods

## Decision Guide
```
Need < 15 min execution? → Lambda
Need containers + simplicity? → ECS Fargate
Need Kubernetes features? → EKS
Need GPU workloads? → EKS or ECS on EC2
Need multi-cloud portability? → EKS
```
