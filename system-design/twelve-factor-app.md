# The Twelve-Factor App

## The 12 Factors

1. **Codebase** – One codebase tracked in VCS, many deploys
2. **Dependencies** – Explicitly declare and isolate dependencies
3. **Config** – Store config in environment variables
4. **Backing services** – Treat them as attached resources
5. **Build, release, run** – Strictly separate build and run stages
6. **Processes** – Execute the app as stateless processes
7. **Port binding** – Export services via port binding
8. **Concurrency** – Scale out via the process model
9. **Disposability** – Fast startup and graceful shutdown
10. **Dev/prod parity** – Keep dev, staging, and prod similar
11. **Logs** – Treat logs as event streams
12. **Admin processes** – Run admin tasks as one-off processes

## Modern Extensions (Beyond 12 Factor)
- **API first** – Design API contract before implementation
- **Telemetry** – Built-in observability (metrics, traces, logs)
- **Security** – Security as a first-class concern
- **Feature flags** – Decouple deployment from release

## Example: Config (Factor #3)
```python
# Bad: hardcoded
DATABASE_URL = "postgres://user:pass@localhost:5432/mydb"

# Good: from environment
DATABASE_URL = os.environ["DATABASE_URL"]
```

## Example: Disposability (Factor #9)
```python
import signal

def graceful_shutdown(signum, frame):
    logger.info("Shutting down gracefully...")
    server.stop(grace=10)  # 10s grace period
    sys.exit(0)

signal.signal(signal.SIGTERM, graceful_shutdown)
```
