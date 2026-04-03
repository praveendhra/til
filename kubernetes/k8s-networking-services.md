# Kubernetes Service Types

## ClusterIP (Default)
Internal-only. Accessible within the cluster.
```yaml
apiVersion: v1
kind: Service
spec:
  type: ClusterIP
  selector:
    app: my-backend
  ports:
  - port: 80
    targetPort: 8080
```
Access as: `http://my-backend.namespace.svc.cluster.local`

## NodePort
Exposes service on each node's IP at a static port (30000-32767).
```yaml
spec:
  type: NodePort
  ports:
  - port: 80
    targetPort: 8080
    nodePort: 30080
```
Access as: `http://<node-ip>:30080`. Rarely used in production.

## LoadBalancer
Provisions cloud load balancer (NLB/ALB on AWS, LB on Azure/GCP).
```yaml
spec:
  type: LoadBalancer
  ports:
  - port: 443
    targetPort: 8080
```
One LB per service = expensive. Use Ingress instead for HTTP.

## Ingress
L7 routing — single LB for multiple services.
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
spec:
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /users
        backend:
          service: { name: user-svc, port: { number: 80 } }
      - path: /orders
        backend:
          service: { name: order-svc, port: { number: 80 } }
```

## Gateway API (New Standard)
Replacing Ingress. More expressive, role-oriented.
```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
spec:
  parentRefs:
  - name: my-gateway
  rules:
  - matches:
    - path: { value: /api }
    backendRefs:
    - name: api-service
      port: 80
```
