# Linux Process Signals

## Common Signals
| Signal | Number | Default Action | Use |
|--------|--------|---------------|-----|
| SIGHUP | 1 | Terminate | Reload config (daemon convention) |
| SIGINT | 2 | Terminate | Ctrl+C |
| SIGQUIT | 3 | Core dump | Ctrl+\ |
| SIGKILL | 9 | Terminate | Force kill (CANNOT be caught) |
| SIGTERM | 15 | Terminate | Graceful shutdown (default `kill`) |
| SIGSTOP | 19 | Stop | Pause process (CANNOT be caught) |
| SIGCONT | 18 | Continue | Resume paused process |
| SIGUSR1/2 | 10/12 | Terminate | User-defined |

## Graceful Shutdown in Containers
Kubernetes sends SIGTERM first, waits `terminationGracePeriodSeconds` (default 30s), then SIGKILL.

```python
import signal
import sys

def graceful_shutdown(signum, frame):
    print("Shutting down gracefully...")
    # Close DB connections, flush buffers, etc.
    sys.exit(0)

signal.signal(signal.SIGTERM, graceful_shutdown)
signal.signal(signal.SIGINT, graceful_shutdown)
```

## Docker
```dockerfile
# Use exec form so PID 1 receives signals
CMD ["python", "app.py"]

# Shell form wraps in /bin/sh — signals go to shell, not app!
# BAD: CMD python app.py
```

## Key Takeaway
Always handle SIGTERM for graceful shutdown. SIGKILL is a last resort — processes can't clean up.
