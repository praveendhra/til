# DNS Resolution Step by Step

## The Journey
```
Browser → OS Cache → Resolver → Root → TLD → Authoritative → IP
```

### 1. Browser Cache
Browser checks its own DNS cache first.

### 2. OS Cache
Operating system's DNS cache (`/etc/hosts` on Linux/Mac).

### 3. Recursive Resolver
ISP's DNS server (or 8.8.8.8, 1.1.1.1).
If cached, returns immediately. Otherwise, starts recursive lookup.

### 4. Root Name Server
13 root server clusters worldwide. Returns TLD server address.
"I don't know google.com, but .com TLD is at 192.5.6.30"

### 5. TLD Name Server
.com, .org, .net, etc. Returns authoritative NS.
"google.com's nameserver is ns1.google.com at 216.239.32.10"

### 6. Authoritative Name Server
Has the actual DNS record. Returns the IP.
"google.com A record → 142.250.80.46"

## Record Types
| Type | Purpose | Example |
|------|---------|---------|
| A | IPv4 address | `1.2.3.4` |
| AAAA | IPv6 address | `2001:db8::1` |
| CNAME | Alias | `www → example.com` |
| MX | Mail server | `mail.example.com` |
| TXT | Text (SPF, DKIM) | `v=spf1 include:...` |
| NS | Nameserver | `ns1.example.com` |
| SRV | Service discovery | Used by K8s, Consul |

## Cloud DNS Services
- **AWS Route 53**: Routing policies (latency, geo, weighted, failover)
- **Azure DNS**: Alias records, private DNS zones
- **GCP Cloud DNS**: 100% SLA, DNSSEC support
