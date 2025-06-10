# WebSockets vs SSE vs Long Polling

## Long Polling
Client sends request → server holds it until data is ready → responds → client immediately sends new request.

**Pros**: Works everywhere, simple
**Cons**: High overhead, not truly real-time

## Server-Sent Events (SSE)
Server pushes events to client over a single HTTP connection.

**Pros**: Simple, auto-reconnect, works with HTTP/2
**Cons**: One-way (server → client only), text only

```
GET /api/events
Accept: text/event-stream

data: {"type": "update", "value": 42}
data: {"type": "update", "value": 43}
```

## WebSockets
Full-duplex bidirectional communication over a single TCP connection.

**Pros**: True bidirectional, binary support, low latency
**Cons**: Harder to scale (stateful), need sticky sessions or pub/sub

## When to Use What
| Use Case | Technology |
|----------|-----------|
| Chat applications | WebSocket |
| Live sports scores | SSE |
| Stock tickers | WebSocket |
| Notifications | SSE |
| Collaborative editing | WebSocket |
| Dashboard updates | SSE |
| Simple status polling | Long Polling |

## Cloud Support
- **AWS**: API Gateway WebSocket, AppSync (GraphQL subscriptions)
- **Azure**: SignalR Service (manages WebSocket complexity)
- **GCP**: Cloud Run supports WebSockets, Firebase Realtime DB
