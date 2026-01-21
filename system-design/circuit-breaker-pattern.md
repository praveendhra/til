# Circuit Breaker Pattern

## Problem
Service A calls Service B. If B is down, A keeps trying, wasting resources and cascading the failure.

## Solution: Circuit Breaker

### States
```
CLOSED  →→→  OPEN  →→→  HALF-OPEN
  ↑                         |
  └─────────────────────────┘
```

1. **CLOSED**: Normal operation. Track failures.
2. **OPEN**: After N failures, stop sending requests. Fail fast. Wait timeout.
3. **HALF-OPEN**: Allow one test request. If success → CLOSED. If fail → OPEN.

## Configuration
```yaml
circuitBreaker:
  failureThreshold: 5       # Open after 5 failures
  successThreshold: 3       # Close after 3 successes in half-open
  timeout: 30000            # 30s before trying half-open
  monitorInterval: 10000    # Check every 10s
```

## Implementations
- **Resilience4j** (Java) — used in Spring Boot
- **Polly** (.NET)
- **Istio** — service mesh level (no code changes)
- **AWS App Mesh** — Envoy-based circuit breaking

## At Work
We use circuit breakers in our microservices to prevent cascading failures during deployments. Combined with retry + exponential backoff for transient errors.
