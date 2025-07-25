# Chaos Engineering

## Definition
Proactively injecting failures to discover weaknesses before they cause real outages.

## Principles (from Netflix)
1. **Define steady state** — what "normal" looks like (latency, error rate, throughput)
2. **Hypothesize** — "If X fails, the system will gracefully degrade"
3. **Introduce realistic failures** — network delays, service crashes, disk full
4. **Minimize blast radius** — start small, in non-prod, then carefully in prod
5. **Observe and learn** — did the system behave as expected?

## Common Experiments
| Experiment | What It Tests |
|-----------|--------------|
| Kill a pod/instance | Auto-recovery, health checks |
| Network latency injection | Timeout handling, circuit breakers |
| DNS failure | Failover, caching |
| CPU/memory stress | Autoscaling, resource limits |
| AZ/region failure | Multi-AZ/region resilience |
| Database failover | Connection retry, read replica promotion |

## Tools
- **AWS FIS** (Fault Injection Simulator): Native AWS chaos tool
- **Azure Chaos Studio**: Native Azure chaos experiments
- **Litmus Chaos**: Kubernetes-native, CNCF project
- **Gremlin**: Enterprise SaaS platform
- **Chaos Monkey** (Netflix): Random instance termination

## My Experience
We run quarterly chaos experiments at work to validate DR and fallback services. Key learnings:
- Circuit breakers must be tuned per-service, not global defaults
- DNS caching can hide failures for minutes
- Always have runbooks before running chaos in prod
