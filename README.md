# TIL (Today I Learned)

A collection of concise, practical notes on things I learn daily across DevOps, cloud infrastructure, system design, and software engineering. Each entry is a deep-dive reference designed for quick review and interview preparation.

## Categories

### System Design (25 entries)
Core concepts for system design interviews — distributed systems, databases, networking, and architecture patterns.

| Topic | Key Concepts |
|-------|-------------|
| [CAP Theorem](system-design/cap-theorem.md) | CP vs AP, PACELC, real-world databases |
| [Consistent Hashing](system-design/consistent-hashing.md) | Virtual nodes, replication, implementation |
| [Rate Limiting](system-design/rate-limiting-algorithms.md) | Token bucket, sliding window, distributed with Redis |
| [Database Sharding](system-design/database-sharding.md) | Strategies, shard key selection, resharding |
| [Database Indexing](system-design/database-indexing-strategies.md) | B-Tree, composite, covering, partial indexes |
| [Caching Strategies](system-design/caching-strategies.md) | Cache-aside, stampede prevention, multi-level |
| [Load Balancing](system-design/load-balancer-algorithms.md) | L4 vs L7, algorithms, GSLB, health checks |
| [Event-Driven Architecture](system-design/event-driven-architecture.md) | Outbox pattern, DLQ, choreography vs orchestration |
| [Microservices Communication](system-design/microservices-communication.md) | REST vs gRPC, circuit breaker, bulkhead |
| [Distributed Consensus](system-design/distributed-consensus.md) | Raft, Paxos, quorum, practical usage |
| [Saga Pattern](system-design/saga-pattern.md) | Orchestration vs choreography, compensation |
| [Message Queues vs Streams](system-design/message-queues-vs-event-streams.md) | SQS vs Kafka, when to use which |
| [API Design](system-design/api-design-best-practices.md) | REST, pagination, versioning, error handling |
| [Back-of-Envelope Estimation](system-design/back-of-envelope-estimation.md) | Essential numbers, estimation framework |
| [URL Shortener Design](system-design/design-url-shortener.md) | Capacity planning, architecture, caching |
| [Circuit Breaker](system-design/circuit-breaker-pattern.md) | States, implementation, when to use |
| [CQRS](system-design/cqrs.md) | Command/query separation |
| [Event Sourcing](system-design/event-sourcing.md) | Event log, state reconstruction |
| [Bloom Filters](system-design/bloom-filters.md) | Probabilistic data structure |
| [Distributed Locking](system-design/distributed-locking.md) | Redis, ZooKeeper, fencing tokens |
| [Leader Election](system-design/leader-election-patterns.md) | Bully, Raft-based, ZooKeeper |
| [CDN Architecture](system-design/cdn-architecture.md) | Edge caching, cache invalidation |
| [SQL vs NoSQL](system-design/sql-vs-nosql-decision-guide.md) | Decision guide |
| [Back Pressure](system-design/back-pressure.md) | Flow control in distributed systems |
| [Twelve-Factor App](system-design/twelve-factor-app.md) | Cloud-native application methodology |

### Kubernetes (8 entries)
Production Kubernetes operations, networking, and troubleshooting.

| Topic | Key Concepts |
|-------|-------------|
| [Networking & Services](kubernetes/k8s-networking-services.md) | CNI, NetworkPolicy, Ingress, DNS |
| [Autoscaling (HPA/VPA/KEDA)](kubernetes/hpa-vs-vpa-vs-keda.md) | All 4 autoscalers, Karpenter |
| [Resource Requests & Limits](kubernetes/resource-limits-and-requests.md) | QoS classes, right-sizing, LimitRange |
| [Troubleshooting Guide](kubernetes/k8s-troubleshooting.md) | Decision trees, debugging commands |
| [Helm Charts](kubernetes/helm-charts.md) | Package management for K8s |
| [RBAC](kubernetes/k8s-rbac.md) | Roles, ClusterRoles, ServiceAccounts |
| [Pod Disruption Budgets](kubernetes/pod-disruption-budgets.md) | Safe eviction policies |
| [Resource Management](kubernetes/k8s-resource-management.md) | Quotas, limits, scheduling |

### AWS (12 entries)
| Topic | Key Concepts |
|-------|-------------|
| [ECS vs EKS vs Lambda](aws/ecs-vs-eks-vs-lambda.md) | Decision framework, cost comparison |
| [DynamoDB Design Patterns](aws/dynamodb-design-patterns.md) | Single-table, GSI, write sharding, streams |
| [IAM & Least Privilege](aws/iam-least-privilege.md) | Policy evaluation, ABAC, IRSA |
| [VPC & Networking](aws/vpc-peering-vs-transit-gateway.md) | VPC design, Transit Gateway |
| [Lambda Cold Starts](aws/lambda-cold-starts.md) | Optimization strategies |
| [S3 Storage Classes](aws/s3-storage-classes.md) | Lifecycle policies, cost optimization |
| [Aurora vs RDS](aws/aurora-vs-rds.md) | When to choose which |
| [EventBridge](aws/eventbridge-patterns.md) | Event routing, rules, targets |
| [Step Functions](aws/aws-step-functions.md) | Workflow orchestration |
| [Secrets Manager](aws/aws-secrets-manager.md) | Secret rotation |
| [CloudFront](aws/aws-cloudfront.md) | CDN, edge functions |
| [Organizations](aws/aws-organizations.md) | Multi-account strategy |

### Azure (15 entries)
| Topic | Key Concepts |
|-------|-------------|
| [Cosmos DB Consistency](azure/cosmos-db-consistency-levels.md) | 5 levels, RU costs, multi-region |
| [App Service vs Functions vs AKS](azure/azure-app-service-vs-functions-vs-aks.md) | Compute decision guide |
| [Networking Overview](azure/azure-networking-overview.md) | VNet, NSG, Azure Firewall |
| [Identity & Access](azure/azure-identity-and-access.md) | Entra ID, RBAC |
| [Managed Identity](azure/azure-managed-identity.md) | Passwordless authentication |
| [DevOps Pipelines](azure/azure-devops-pipelines.md) | CI/CD best practices |
| [Key Vault](azure/azure-key-vault.md) | Secret management |
| [Monitor & App Insights](azure/azure-monitor-and-app-insights.md) | Observability |
| [Container Apps](azure/azure-container-apps.md) | Serverless containers |
| [Service Bus](azure/azure-service-bus.md) | Messaging patterns |
| [Front Door](azure/azure-front-door.md) | Global load balancing |
| [Durable Functions](azure/azure-functions-durable.md) | Workflow patterns |
| [Bicep](azure/azure-bicep.md) | IaC for Azure |
| [Policy](azure/azure-policy.md) | Governance |
| [Storage Options](azure/azure-storage-options.md) | Blob, File, Queue, Table |

### GCP (15 entries)
| Topic | Key Concepts |
|-------|-------------|
| [Compute Options](gcp/gcp-compute-options.md) | GCE, GKE, Cloud Run, Functions |
| [Networking VPC](gcp/gcp-networking-vpc.md) | Shared VPC, firewall rules |
| [IAM & Service Accounts](gcp/gcp-iam-and-service-accounts.md) | Workload Identity |
| [Cloud Spanner](gcp/cloud-spanner.md) | Globally distributed SQL |
| [Pub/Sub](gcp/pub-sub-patterns.md) | Messaging patterns |
| [BigQuery](gcp/bigquery-tips.md) | Data warehouse tips |
| [Cloud Build](gcp/gcp-cloud-build.md) | CI/CD |
| [Cloud Functions v2](gcp/gcp-cloud-functions-v2.md) | Serverless |
| [Cloud SQL vs AlloyDB vs Spanner](gcp/cloud-sql-vs-alloydb-vs-spanner.md) | Database decision |
| [Firestore vs Bigtable](gcp/firestore-vs-bigtable.md) | NoSQL options |
| [Load Balancing](gcp/gcp-load-balancing.md) | Global, regional |
| [Secret Manager](gcp/gcp-secret-manager.md) | Secrets management |
| [Artifact Registry](gcp/gcp-artifact-registry.md) | Container registry |
| [Workload Identity](gcp/gcp-workload-identity.md) | GKE IAM integration |
| [Pub/Sub Patterns](gcp/gcp-pub-sub.md) | Advanced patterns |

### DevOps & SRE (12 entries)
| Topic | Key Concepts |
|-------|-------------|
| [SRE Golden Signals](devops/sre-golden-signals.md) | USE/RED, SLI/SLO/SLA, alerting |
| [Deployment Strategies](devops/blue-green-vs-canary-vs-rolling.md) | Canary, blue-green, Argo Rollouts |
| [Observability](devops/observability-three-pillars.md) | Metrics, logs, traces, OpenTelemetry |
| [Incident Management](devops/incident-management.md) | Response framework, post-mortem |
| [GitOps](devops/gitops-principles.md) | ArgoCD, Flux, principles |
| [CI/CD Best Practices](devops/infrastructure-as-code-comparison.md) | IaC comparison |
| [Chaos Engineering](devops/chaos-engineering-principles.md) | Principles and tools |
| [Feature Flags](devops/feature-flags.md) | Strategies and implementation |
| [Container Security](devops/container-security.md) | Best practices |
| [Zero Trust Security](devops/zero-trust-security.md) | Architecture |
| [SLI/SLO/SLA](devops/sli-slo-sla.md) | Error budgets |
| [Infrastructure Testing](devops/infrastructure-testing.md) | Testing pyramid |

### Networking (2 entries)
| Topic | Key Concepts |
|-------|-------------|
| [TCP/IP, HTTP, TLS](networking/tcp-ip-http-deep-dive.md) | Protocols, handshakes, HTTP/2 vs 3 |
| [DNS Resolution](networking/dns-resolution-explained.md) | Record types, resolution flow |

### Terraform (2 entries)
| Topic | Key Concepts |
|-------|-------------|
| [State Management](terraform/terraform-state-management.md) | Remote backend, locking, workspaces |
| [Modules Best Practices](terraform/terraform-modules-best-practices.md) | Reusable module patterns |

### Docker (1 entry)
| Topic | Key Concepts |
|-------|-------------|
| [Multi-Stage Builds & Security](docker/multi-stage-builds.md) | Layer caching, minimal images, security |

### Linux (2 entries)
| Topic | Key Concepts |
|-------|-------------|
| [Performance Troubleshooting](linux/linux-performance-troubleshooting.md) | USE method, essential commands |
| [Process Signals](linux/linux-process-signals.md) | SIGTERM, SIGKILL, signal handling |

### Python (2 entries)
| Topic | Key Concepts |
|-------|-------------|
| [Asyncio Basics](python/python-asyncio-basics.md) | Event loop, async/await |
| [Context Managers](python/python-context-managers.md) | with statement, resource management |

### Git (1 entry)
| Topic | Key Concepts |
|-------|-------------|
| [Rebase vs Merge](git/git-rebase-vs-merge.md) | When to use which |

---

*Total: 105+ entries across 12 categories. Updated regularly.*

### AI & Machine Learning (15 entries)
Deep dives into modern AI — LLMs, agents, RAG, and production AI systems.

| Topic | Key Concepts |
|-------|-------------|
| [Transformer Architecture](ai/transformer-architecture.md) | Self-attention, multi-head, encoder vs decoder |
| [Prompt Engineering](ai/prompt-engineering.md) | Few-shot, CoT, ReAct, temperature tuning |
| [RAG](ai/rag-retrieval-augmented-generation.md) | Chunking, embeddings, vector search, hybrid retrieval |
| [AI Agents & Tool Use](ai/ai-agents-and-tool-use.md) | ReAct loop, function calling, multi-agent |
| [LangChain & LangGraph](ai/langchain-and-langgraph.md) | LCEL, stateful workflows, human-in-the-loop |
| [Vector Databases](ai/vector-databases-and-embeddings.md) | HNSW, IVF, Pinecone vs Qdrant, similarity search |
| [Fine-Tuning vs RAG](ai/fine-tuning-vs-prompting-vs-rag.md) | Decision framework, LoRA, QLoRA |
| [MCP Protocol](ai/mcp-model-context-protocol.md) | Tools, resources, prompts, server implementation |
| [LLM Evaluation](ai/llm-evaluation-and-testing.md) | RAGAS, LLM-as-judge, CI testing |
| [AI Safety & Guardrails](ai/ai-safety-and-guardrails.md) | Prompt injection, output filtering, NeMo |
| [Multi-Agent Systems](ai/multi-agent-systems.md) | CrewAI, orchestration patterns, collaboration |
| [Tokenization & Context](ai/tokenization-and-context-windows.md) | BPE, context limits, cost management |
| [LLMOps](ai/llmops-ai-in-production.md) | Latency, caching, model routing, monitoring |
| [Structured Output](ai/structured-output-and-function-calling.md) | JSON mode, Pydantic, Instructor, parallel calls |
| [Open Source LLMs](ai/open-source-llms-and-self-hosting.md) | vLLM, Ollama, quantization, K8s deployment |
