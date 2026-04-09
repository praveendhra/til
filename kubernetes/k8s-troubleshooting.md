# Kubernetes Troubleshooting Guide

## Pod Not Starting — Decision Tree

```
Pod not running?
  │
  ├─ Phase: Pending
  │   ├─ "Insufficient cpu/memory" → Not enough node resources
  │   │   → Check: kubectl describe pod → Events
  │   │   → Fix: Cluster Autoscaler, or reduce resource requests
  │   ├─ "no nodes match pod topology spread constraints" → Topology constraint
  │   ├─ "Unschedulable" → Node taint/affinity mismatch
  │   │   → Check: kubectl get nodes -o json | jq '.items[].spec.taints'
  │   └─ PVC pending → StorageClass issue or no available volumes
  │
  ├─ Phase: Waiting (ContainerCreating)
  │   ├─ ImagePullBackOff → Wrong image name, tag, or registry auth
  │   │   → Check: kubectl describe pod → Events → "Failed to pull image"
  │   │   → Fix: Verify image exists, check imagePullSecrets
  │   └─ Creating container → Waiting for volume mount, init containers
  │
  ├─ Phase: Running but CrashLoopBackOff
  │   ├─ Check current logs: kubectl logs <pod>
  │   ├─ Check previous crash: kubectl logs <pod> --previous
  │   ├─ Common causes:
  │   │   ├─ App error (check stack trace in logs)
  │   │   ├─ Missing config/secret (env vars, mounted files)
  │   │   ├─ OOMKilled (memory limit too low)
  │   │   │   → Check: kubectl describe pod → State → OOMKilled
  │   │   │   → Fix: Increase memory limit
  │   │   ├─ Liveness probe failing
  │   │   │   → Fix: Increase initialDelaySeconds
  │   │   └─ Port conflict or wrong command
  │   └─ Debug: kubectl exec -it <pod> -- sh (if container runs)
  │
  └─ Phase: Running but not Ready
      ├─ Readiness probe failing
      │   → Check: kubectl describe pod → Conditions → Ready: False
      │   → Check probe endpoint: kubectl exec <pod> -- curl localhost:8080/healthz
      └─ Pod is running but app isn't serving traffic

```

## Essential Debugging Commands

```bash
# 1. Overview of what's wrong
kubectl get pods -n <ns> -o wide
kubectl get events -n <ns> --sort-by='.lastTimestamp' | tail -20

# 2. Deep dive into a pod
kubectl describe pod <pod> -n <ns>    # Shows events, conditions, mounts
kubectl logs <pod> -n <ns>            # Current container logs
kubectl logs <pod> -n <ns> --previous # Previous crash logs
kubectl logs <pod> -n <ns> -c <container>  # Specific container (multi-container)

# 3. Get into the pod
kubectl exec -it <pod> -n <ns> -- /bin/sh
# If no shell: use ephemeral debug container
kubectl debug -it <pod> --image=busybox --target=<container>

# 4. Check resource usage
kubectl top pods -n <ns> --sort-by=cpu
kubectl top nodes

# 5. Check networking
kubectl get svc -n <ns>
kubectl get endpoints <service> -n <ns>  # Are pod IPs listed?
kubectl get ingress -n <ns>

# 6. DNS debugging
kubectl run -it --rm debug --image=busybox -- nslookup <service>.<ns>.svc.cluster.local

# 7. Node issues
kubectl describe node <node>
kubectl get nodes -o custom-columns=NAME:.metadata.name,STATUS:.status.conditions[-1].type,REASON:.status.conditions[-1].reason
```

## Common Issues & Solutions

### 1. ImagePullBackOff
```bash
# Check events
kubectl describe pod <pod> | grep -A5 Events

# Private registry? Create/check imagePullSecrets
kubectl create secret docker-registry regcred \
  --docker-server=<registry> \
  --docker-username=<user> \
  --docker-password=<token>
```

### 2. OOMKilled (Exit Code 137)
```bash
# Check if pod was OOM killed
kubectl describe pod <pod> | grep -i oom
kubectl get pod <pod> -o jsonpath='{.status.containerStatuses[0].lastState}'

# Fix: Increase memory limit
resources:
  limits:
    memory: 1Gi  # was 512Mi
```

### 3. Service Not Reaching Pods
```bash
# Check endpoints (should list pod IPs)
kubectl get endpoints <service>

# Empty endpoints? Labels don't match!
kubectl get pods --show-labels
kubectl get svc <service> -o jsonpath='{.spec.selector}'

# Test connectivity from another pod
kubectl exec -it <other-pod> -- curl <service>:<port>
```

### 4. PVC Stuck in Pending
```bash
kubectl describe pvc <pvc>
# Common: No StorageClass, no available volumes, wrong access mode

# Check StorageClasses
kubectl get sc
```

### 5. Node NotReady
```bash
kubectl describe node <node> | grep -A10 Conditions
# Common: kubelet crashed, disk pressure, memory pressure, network

# Check kubelet logs (SSH to node)
journalctl -u kubelet -f
```

### 6. Pod Evicted
```bash
kubectl get pods --field-selector=status.phase=Failed | grep Evicted
# Cause: Node disk pressure or memory pressure
# Clean up: kubectl delete pods --field-selector=status.phase=Failed
```

## Resource Debugging Cheat Sheet

```bash
# Find pods using most CPU
kubectl top pods -A --sort-by=cpu | head -20

# Find pods using most memory
kubectl top pods -A --sort-by=memory | head -20

# Find pods without resource requests (bad practice)
kubectl get pods -A -o json | jq -r \
  '.items[] | select(.spec.containers[].resources.requests == null) | .metadata.name'

# Find pods in error state across all namespaces
kubectl get pods -A --field-selector=status.phase!=Running,status.phase!=Succeeded

# Recent events with warnings
kubectl get events -A --field-selector type=Warning --sort-by='.lastTimestamp' | tail -30
```

## Network Debugging

```bash
# Check if DNS is working
kubectl run -it --rm dns-test --image=busybox -- nslookup kubernetes.default

# Check if service is reachable
kubectl run -it --rm net-test --image=curlimages/curl -- curl -v http://<service>.<ns>:8080/health

# Check NetworkPolicy blocking traffic
kubectl get networkpolicies -n <ns>
kubectl describe networkpolicy <policy> -n <ns>

# Port forwarding for local debugging
kubectl port-forward svc/<service> 8080:80 -n <ns>
```

## Interview Tip

When asked "how would you troubleshoot a pod in CrashLoopBackOff?":

> "First, I'd check `kubectl describe pod` for events — this tells me if it's an image pull issue, resource constraint, or runtime error. Then `kubectl logs --previous` to see the last crash output. If it's OOMKilled (exit code 137), I'd increase the memory limit. If it's an application error, I'd check the stack trace and verify ConfigMaps/Secrets are correctly mounted. For liveness probe failures, I'd increase `initialDelaySeconds` for slow-starting apps. As a last resort, I'd use `kubectl debug` to attach an ephemeral debug container."
