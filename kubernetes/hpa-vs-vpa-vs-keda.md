# Kubernetes Autoscaling — HPA, VPA, KEDA, Cluster Autoscaler

## The Four Types of Autoscaling in K8s

```
                    ┌─────────────────────────────┐
                    │    Cluster Autoscaler        │
                    │ (Adds/removes NODES)         │
                    └──────────────┬──────────────┘
                                   │
              ┌────────────────────┼────────────────────┐
              │                    │                    │
    ┌─────────┴──────┐   ┌────────┴───────┐   ┌───────┴────────┐
    │   HPA          │   │   VPA          │   │   KEDA         │
    │ (Scale pods    │   │ (Right-size    │   │ (Scale on      │
    │  horizontally) │   │  pod resources)│   │  events/custom)│
    └────────────────┘   └────────────────┘   └────────────────┘
```

## HPA (Horizontal Pod Autoscaler) — Most Common

Adjusts the **number of replicas** based on observed metrics.

### Basic CPU/Memory Scaling
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: web-app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: web-app
  minReplicas: 3
  maxReplicas: 50
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70  # Scale up when avg CPU > 70%
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300  # Wait 5 min before scaling down
      policies:
        - type: Pods
          value: 2
          periodSeconds: 60  # Remove max 2 pods per minute
    scaleUp:
      stabilizationWindowSeconds: 0  # Scale up immediately
      policies:
        - type: Percent
          value: 100
          periodSeconds: 60  # Double pods per minute (max)
```

### Custom Metrics (Prometheus)
```yaml
metrics:
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: 1000  # Target 1000 RPS per pod
  - type: Object
    object:
      metric:
        name: queue_depth
      describedObject:
        apiVersion: v1
        kind: Service
        name: rabbitmq
      target:
        type: Value
        value: 100  # Scale when queue > 100 messages
```

### HPA Algorithm

```
desiredReplicas = ceil(currentReplicas × (currentMetric / targetMetric))

Example:
  Current: 3 replicas, 90% CPU
  Target: 70% CPU
  Desired = ceil(3 × (90/70)) = ceil(3.86) = 4 replicas
```

### Important: Resource Requests Are REQUIRED

HPA CPU scaling only works if pods have CPU requests defined. Without requests, HPA can't calculate utilization percentage.

```yaml
resources:
  requests:
    cpu: 250m     # REQUIRED for HPA to work
    memory: 256Mi
  limits:
    cpu: 500m
    memory: 512Mi
```

## VPA (Vertical Pod Autoscaler)

Adjusts **CPU and memory requests** per pod based on actual usage.

```yaml
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: web-app-vpa
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: web-app
  updatePolicy:
    updateMode: "Auto"  # Options: "Off", "Initial", "Recreate", "Auto"
  resourcePolicy:
    containerPolicies:
      - containerName: web-app
        minAllowed:
          cpu: 100m
          memory: 128Mi
        maxAllowed:
          cpu: 2
          memory: 2Gi
```

### VPA Modes

| Mode | Behavior |
|------|----------|
| **Off** | Only recommends, doesn't apply (safest to start) |
| **Initial** | Sets resources only at pod creation, never updates running pods |
| **Recreate** | Evicts and recreates pods to apply new resources |
| **Auto** | Currently same as Recreate |

### ⚠️ VPA Gotchas
- VPA **restarts pods** to apply changes (requires PDB for safe rollouts)
- **Don't use VPA and HPA on the same metric** (CPU) — they'll fight
- VPA works well with HPA when: VPA handles memory, HPA handles CPU
- Or: Use VPA in "Off" mode for recommendations, apply manually

## KEDA (Kubernetes Event-Driven Autoscaling)

Scale based on **external event sources**: queue depth, Kafka lag, cron, HTTP rate, CloudWatch metrics, Prometheus queries, and 50+ other scalers.

### Key Superpower: Scale to ZERO

```yaml
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: order-processor
spec:
  scaleTargetRef:
    name: order-processor
  minReplicaCount: 0    # ← Scale to ZERO when no events!
  maxReplicaCount: 30
  cooldownPeriod: 300   # Wait 5 min before scaling to 0
  pollingInterval: 15   # Check every 15 seconds
  triggers:
    - type: azure-servicebus
      metadata:
        queueName: orders
        messageCount: "5"  # 1 pod per 5 messages
        connectionFromEnv: SB_CONNECTION_STRING
    - type: cron
      metadata:
        timezone: America/New_York
        start: "0 8 * * 1-5"   # Mon-Fri 8 AM
        end: "0 18 * * 1-5"    # Mon-Fri 6 PM
        desiredReplicas: "3"   # Ensure 3 pods during business hours
```

### Popular KEDA Scalers

| Scaler | Use Case |
|--------|----------|
| **Kafka** | Scale by consumer group lag |
| **RabbitMQ** | Scale by queue depth |
| **SQS** | Scale by queue messages |
| **Prometheus** | Scale on any Prometheus query |
| **Cron** | Time-based scaling |
| **Azure Service Bus** | Scale by queue/topic messages |
| **PostgreSQL** | Scale by row count (e.g., pending jobs) |
| **HTTP** | Scale by RPS via KEDA HTTP add-on |
| **CPU/Memory** | Enhanced version of HPA |

## Cluster Autoscaler

Adds or removes **nodes** when:
- Pods can't be scheduled (not enough resources) → add nodes
- Nodes are underutilized (< 50% for 10 min) → remove nodes

```
Pod pending (no schedulable node)
  → Cluster Autoscaler detects
  → Provisions new node from cloud provider
  → Node joins cluster (2-5 minutes)
  → Pod scheduled on new node
```

**Managed options**: EKS Cluster Autoscaler, GKE Node Auto-provisioning, AKS Cluster Autoscaler, **Karpenter** (AWS, faster than Cluster Autoscaler)

### Karpenter vs Cluster Autoscaler

| Feature | Cluster Autoscaler | Karpenter |
|---------|-------------------|-----------|
| Speed | 2-5 min | 30-90 sec |
| Node groups | Pre-defined | Dynamic (selects best fit) |
| Instance types | Fixed per group | Flexible per pod |
| Cloud support | AWS, GCP, Azure | AWS (Azure in preview) |
| Consolidation | Limited | Automatic bin-packing |

## Decision Matrix

| Scenario | Solution |
|----------|----------|
| Web API with variable traffic | HPA on CPU + Cluster Autoscaler |
| Queue processor (bursty) | KEDA (scale to zero when idle) |
| Batch jobs | KEDA with Cron + queue triggers |
| Right-sizing resource requests | VPA in "Off" mode for recommendations |
| Cost optimization | Karpenter + Spot instances |
| Memory-intensive workloads | VPA for memory + HPA for CPU |

## Interview Answer

> "I use HPA for most stateless services, scaling on CPU utilization with a target of 70%. For event-driven workloads like queue processors, KEDA is essential because it can scale to zero, saving costs when there's no work. I pair these with Karpenter (on AWS) for node-level autoscaling — it's faster than the Cluster Autoscaler and automatically selects the best instance types. For right-sizing, I run VPA in recommendation-only mode to identify over-provisioned pods. The key is setting proper resource requests — without them, HPA can't calculate utilization and the scheduler can't make good placement decisions."
