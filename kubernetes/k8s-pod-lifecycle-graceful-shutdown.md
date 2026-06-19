# Kubernetes Pod Lifecycle and Graceful Shutdown

## Pod Phases

```
Pending → Running → Succeeded/Failed
                  → Unknown (node lost)
```

## Container States

| State | Meaning |
|-------|---------|
| Waiting | Pulling image, creating container |
| Running | Executing without issues |
| Terminated | Finished (exit code 0) or crashed |

## Startup, Liveness, and Readiness Probes

```yaml
containers:
  - name: app
    livenessProbe:          # Is the process alive? Restart if not.
      httpGet:
        path: /healthz
        port: 8080
      initialDelaySeconds: 10
      periodSeconds: 10
      failureThreshold: 3

    readinessProbe:         # Can it serve traffic? Remove from LB if not.
      httpGet:
        path: /ready
        port: 8080
      periodSeconds: 5
      failureThreshold: 2

    startupProbe:           # Still starting up? Don't kill it yet.
      httpGet:
        path: /healthz
        port: 8080
      failureThreshold: 30
      periodSeconds: 2      # gives up to 60s to start
```

## Graceful Shutdown Sequence

When a pod is terminated:

```
1. Pod marked as Terminating
2. Endpoints controller removes pod from Service (async!)
3. preStop hook executes (if defined)
4. SIGTERM sent to PID 1
5. Wait terminationGracePeriodSeconds (default: 30s)
6. SIGKILL if still running
```

**Critical issue**: Steps 2 and 3-4 happen in parallel. Traffic can still arrive briefly after SIGTERM.

## Proper Graceful Shutdown

```yaml
containers:
  - name: app
    lifecycle:
      preStop:
        exec:
          command: ["sh", "-c", "sleep 5"]  # wait for LB to drain
    terminationGracePeriodSeconds: 60
```

Application code should:
```python
import signal
import sys

def handle_sigterm(signum, frame):
    # Stop accepting new requests
    server.stop_accepting()
    # Finish in-flight requests (with timeout)
    server.drain(timeout=25)
    sys.exit(0)

signal.signal(signal.SIGTERM, handle_sigterm)
```

## Init Containers

Run before main containers, one at a time, must succeed:

```yaml
initContainers:
  - name: wait-for-db
    image: busybox
    command: ["sh", "-c", "until nc -z db-service 5432; do sleep 2; done"]

  - name: run-migrations
    image: myapp:latest
    command: ["python", "manage.py", "migrate"]
```

## Pod Disruption Budgets

Protect availability during voluntary disruptions (node drain, upgrades):

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: api-pdb
spec:
  minAvailable: 2          # or use maxUnavailable: 1
  selector:
    matchLabels:
      app: api
```

## Common Debugging

```bash
kubectl describe pod <name>         # events, conditions
kubectl logs <name> --previous      # logs from crashed container
kubectl get pod <name> -o yaml      # full spec + status
kubectl exec -it <name> -- sh       # shell into container
```
