# TCP/IP, HTTP, and TLS — Networking Fundamentals

## The TCP/IP Model

```
┌─────────────────┐
│  Application    │  HTTP, gRPC, DNS, SMTP, WebSocket
├─────────────────┤
│  Transport      │  TCP (reliable), UDP (fast)
├─────────────────┤
│  Network/IP     │  IPv4, IPv6, ICMP, routing
├─────────────────┤
│  Link           │  Ethernet, WiFi, ARP
└─────────────────┘
```

## TCP Three-Way Handshake

```
Client                          Server
  │                               │
  │──── SYN (seq=x) ────────────►│  "I want to connect"
  │                               │
  │◄──── SYN-ACK (seq=y, ack=x+1)│  "OK, I accept"
  │                               │
  │──── ACK (ack=y+1) ──────────►│  "Let's go"
  │                               │
  │◄═══════ Data flow ══════════►│
  │                               │

Total: 1.5 round trips before data flows
With 40ms RTT: ~60ms just for the handshake
```

### Connection Teardown (Four-Way)
```
Client                          Server
  │──── FIN ────────────────────►│  "I'm done sending"
  │◄──── ACK ────────────────────│  "Acknowledged"
  │◄──── FIN ────────────────────│  "I'm done too"
  │──── ACK ────────────────────►│  "Bye"
```

## TCP vs UDP

| Feature | TCP | UDP |
|---------|-----|-----|
| Connection | Connection-oriented | Connectionless |
| Reliability | Guaranteed delivery, ordering | Best effort |
| Flow control | Yes (sliding window) | No |
| Overhead | Higher (headers, handshake) | Lower |
| Use cases | HTTP, SSH, database | DNS, video streaming, gaming, VoIP |

## TLS Handshake (HTTPS)

```
Client                          Server
  │                               │
  │──── ClientHello ─────────────►│  Supported cipher suites, TLS version
  │                               │
  │◄──── ServerHello ─────────────│  Chosen cipher suite
  │◄──── Certificate ─────────────│  Server's public certificate
  │◄──── ServerHelloDone ─────────│
  │                               │
  │──── ClientKeyExchange ───────►│  Pre-master secret (encrypted)
  │──── ChangeCipherSpec ────────►│  "Switching to encrypted"
  │──── Finished ────────────────►│
  │                               │
  │◄──── ChangeCipherSpec ────────│
  │◄──── Finished ────────────────│
  │                               │
  │◄═══ Encrypted Data Flow ═══►│

TLS 1.2: 2 round trips (TCP handshake + TLS handshake)
TLS 1.3: 1 round trip (combines steps)
```

**Total connection setup time:**
```
TCP handshake:  1.5 RTT
TLS 1.2:       2 RTT
Total:          3.5 RTT × 40ms = 140ms before first byte!

TLS 1.3:       1 RTT
Total:          2.5 RTT × 40ms = 100ms (faster!)
```

## HTTP/1.1 vs HTTP/2 vs HTTP/3

| Feature | HTTP/1.1 | HTTP/2 | HTTP/3 |
|---------|----------|--------|--------|
| Transport | TCP | TCP | QUIC (UDP-based) |
| Multiplexing | ❌ (1 req per connection) | ✅ (many over 1 connection) | ✅ |
| Header compression | ❌ | ✅ (HPACK) | ✅ (QPACK) |
| Server push | ❌ | ✅ | ✅ |
| Head-of-line blocking | Per connection | TCP-level only | ❌ None! |
| Connection setup | TCP + TLS | TCP + TLS | QUIC (built-in TLS, 0-RTT) |

### HTTP/2 Multiplexing
```
HTTP/1.1: Sequential (or multiple connections)
  Connection 1: [Request A][Response A][Request B][Response B]
  Connection 2: [Request C][Response C]...
  (6 connections max per domain)

HTTP/2: Multiplexed streams over single connection
  Connection 1: [A][C][B][A][C][B][A]...
  (interleaved, all on one connection)
```

## DNS Resolution

```
Browser                     DNS
  │                          │
  │──── "api.example.com" ──►│ Local resolver (ISP or 8.8.8.8)
  │                          │──► Root server (.)
  │                          │◄── "Ask .com nameserver"
  │                          │──► .com TLD server
  │                          │◄── "Ask ns1.example.com"
  │                          │──► Authoritative nameserver
  │                          │◄── "A record: 93.184.216.34"
  │◄──── 93.184.216.34 ─────│
  │                          │
  (Cached for TTL duration, typically 300s)
```

### DNS Record Types

| Type | Purpose | Example |
|------|---------|---------|
| **A** | IPv4 address | `api.example.com → 93.184.216.34` |
| **AAAA** | IPv6 address | `api.example.com → 2606:2800::1` |
| **CNAME** | Alias to another domain | `www.example.com → example.com` |
| **MX** | Mail server | `example.com → mail.example.com` |
| **TXT** | Text data (SPF, DKIM, verification) | `v=spf1 include:_spf.google.com` |
| **NS** | Nameserver | `example.com → ns1.example.com` |
| **SRV** | Service discovery | `_http._tcp.example.com → 10 0 80 web.example.com` |

## Important Port Numbers

| Port | Protocol | Service |
|------|----------|---------|
| 22 | TCP | SSH |
| 53 | TCP/UDP | DNS |
| 80 | TCP | HTTP |
| 443 | TCP | HTTPS |
| 5432 | TCP | PostgreSQL |
| 3306 | TCP | MySQL |
| 6379 | TCP | Redis |
| 8080 | TCP | HTTP alt (common for APIs) |
| 9090 | TCP | Prometheus |
| 27017 | TCP | MongoDB |

## Interview Answer

> "Understanding the network stack is critical for debugging latency issues. A new HTTPS connection requires a TCP handshake (1.5 RTT) plus TLS handshake (1-2 RTT), so with 40ms RTT, that's 100-140ms before the first byte of data. This is why HTTP/2 multiplexing is important — it eliminates the need for multiple TCP connections by interleaving requests on a single connection. HTTP/3 goes further with QUIC, which eliminates TCP head-of-line blocking and supports 0-RTT reconnection. For DNS, TTL management and using GeoDNS (like Route 53) is critical for global latency optimization."
