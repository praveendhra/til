# Python asyncio

## When to Use async
- **I/O-bound tasks**: HTTP requests, DB queries, file I/O
- **NOT for CPU-bound**: Use `multiprocessing` or `concurrent.futures` instead

## Basic Pattern
```python
import asyncio
import aiohttp

async def fetch_url(session, url):
    async with session.get(url) as response:
        return await response.json()

async def main():
    urls = ["https://api.example.com/1", "https://api.example.com/2"]
    async with aiohttp.ClientSession() as session:
        tasks = [fetch_url(session, url) for url in urls]
        results = await asyncio.gather(*tasks)
        print(results)

asyncio.run(main())
```

## Key Concepts
- `async def` — defines a coroutine
- `await` — yield control to event loop until result is ready
- `asyncio.gather()` — run multiple coroutines concurrently
- `asyncio.create_task()` — schedule coroutine without awaiting immediately

## Common Pitfalls
1. **Blocking the event loop**: Don't call `time.sleep()`, use `asyncio.sleep()`
2. **CPU-bound in async**: Offload to thread pool
   ```python
   result = await asyncio.to_thread(cpu_heavy_function, args)
   ```
3. **Forgetting to await**: Coroutine objects are not executed until awaited

## Performance
10 sequential HTTP requests: ~5 seconds
10 concurrent with asyncio: ~0.5 seconds (10x faster)
