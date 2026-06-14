# mTLS and Certificate Management

## TLS vs mTLS

- **TLS**: Client verifies server identity (one-way). Standard HTTPS.
- **mTLS**: Both sides verify each other. Server also checks client certificate.

```
TLS:    Client ---verify---> Server cert (signed by public CA)
mTLS:   Client ---verify---> Server cert
        Server ---verify---> Client cert (signed by internal CA)
```

## When to Use mTLS

- Service-to-service communication (zero trust)
- API authentication (alternative to API keys)
- Internal microservices behind a service mesh
- IoT device authentication

## Certificate Chain

```
Root CA (offline, air-gapped)
  └── Intermediate CA (issues certs)
        ├── Server cert (api.internal.company.com)
        └── Client cert (service-orders)
```

## Generating Certs with OpenSSL

```bash
# Create CA
openssl req -x509 -newkey rsa:4096 -days 3650 \
  -keyout ca-key.pem -out ca-cert.pem -subj "/CN=Internal CA"

# Generate server cert
openssl req -newkey rsa:2048 -keyout server-key.pem -out server-req.pem \
  -subj "/CN=api.internal.svc"
openssl x509 -req -in server-req.pem -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out server-cert.pem -days 365 \
  -extfile <(echo "subjectAltName=DNS:api.internal.svc,DNS:localhost")

# Generate client cert
openssl req -newkey rsa:2048 -keyout client-key.pem -out client-req.pem \
  -subj "/CN=service-orders"
openssl x509 -req -in client-req.pem -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out client-cert.pem -days 365
```

## mTLS in Practice (Python)

```python
import httpx

# Client making mTLS request
client = httpx.Client(
    cert=("client-cert.pem", "client-key.pem"),
    verify="ca-cert.pem",  # verify server's cert against our CA
)
response = client.get("https://api.internal.svc/data")
```

## Certificate Rotation Strategies

| Strategy | How it works |
|----------|--------------|
| Short-lived certs | Issue certs valid for hours/days, auto-renew (SPIFFE/SPIRE) |
| Dual certs | Load new cert before old expires, accept both during overlap |
| cert-manager (K8s) | Automatic issuance and renewal via CRDs |

## Common Pitfalls

- **SAN mismatch**: Certificate must include the hostname in Subject Alternative Names, not just CN
- **Clock skew**: Cert validity depends on accurate time (`ntpd`/`chronyd`)
- **Expired intermediates**: Clients need the full chain, not just the leaf cert
- **Revocation**: Use short-lived certs + OCSP stapling rather than CRLs

## Tools

- **cert-manager**: Kubernetes-native certificate management
- **SPIFFE/SPIRE**: Identity framework for workloads, issues short-lived X.509 SVIDs
- **Vault PKI**: HashiCorp Vault as internal CA with automatic rotation
- **step-ca**: Lightweight ACME-compatible CA for internal use
