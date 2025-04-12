# Real-Time Communication: WebSocket vs SSE vs Polling

## Comparison

| Feature | WebSocket | SSE | Long Polling |
|---------|-----------|-----|-------------|
| Direction | Bidirectional | Server → Client | Client → Server |
| Protocol | ws:// / wss:// | HTTP | HTTP |
| Connection | Persistent | Persistent | Repeated |
| Browser Support | All modern | All modern | Universal |
| Reconnection | Manual | Auto | Manual |
| Binary data | Yes | No (text only) | Yes |

## When to Use What

### WebSocket
- Chat applications
- Multiplayer games
- Collaborative editing
- Trading platforms (need bidirectional)

### Server-Sent Events (SSE)
- Live feeds / notifications
- Stock tickers (one-way updates)
- Progress updates
- Dashboard real-time data

### Long Polling
- Legacy browser support needed
- Simple notification systems
- Infrequent updates

## SSE Example (simple and underrated)
```python
# Server (FastAPI)
@app.get("/events")
async def events():
    async def generate():
        while True:
            data = await get_update()
            yield f"data: {json.dumps(data)}\n\n"
    return StreamingResponse(generate(), media_type="text/event-stream")

# Client (JavaScript)
const source = new EventSource("/events");
source.onmessage = (event) => console.log(JSON.parse(event.data));
```
