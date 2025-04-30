# Deployment Strategies

## Rolling Update
Replace instances one-by-one.
```
v1 v1 v1 v1  →  v2 v1 v1 v1  →  v2 v2 v1 v1  →  v2 v2 v2 v2
```
- **Pro**: Zero downtime, gradual rollout
- **Con**: Two versions running simultaneously
- **K8s**: Default strategy for Deployments

## Blue-Green
Run two identical environments. Switch traffic all at once.
```
[Blue - v1] ← Traffic
[Green - v2] (idle, ready)

Switch DNS/LB:
[Blue - v1] (idle)
[Green - v2] ← Traffic
```
- **Pro**: Instant rollback (switch back)
- **Con**: 2x infrastructure cost during deployment
- **Cloud**: App Service slots (Azure), CodeDeploy (AWS)

## Canary
Route small % of traffic to new version. Gradually increase if healthy.
```
v1 ← 95% traffic
v2 ← 5% traffic (canary)

If healthy: v1 ← 50%, v2 ← 50%
If healthy: v2 ← 100%
```
- **Pro**: Minimize blast radius, real production validation
- **Con**: Complex routing, monitoring needed
- **Tools**: Istio, Flagger, Argo Rollouts, AWS AppConfig

## Feature Flags
Not a deployment strategy per se, but decouple deployment from release.
```python
if feature_flags.is_enabled("new_checkout", user_id):
    new_checkout_flow()
else:
    old_checkout_flow()
```
- **Tools**: LaunchDarkly, Split.io, AWS AppConfig

## My Preference
Canary with automated rollback. Use Argo Rollouts in K8s with Prometheus metrics analysis.
