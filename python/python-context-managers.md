# Python Context Managers

## The Basics
```python
# File handling (most common)
with open("file.txt") as f:
    data = f.read()
# File is automatically closed, even if exception occurs
```

## Custom Context Manager (Class)
```python
class DatabaseConnection:
    def __enter__(self):
        self.conn = create_connection()
        return self.conn

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.conn.close()
        return False  # Don't suppress exceptions

with DatabaseConnection() as conn:
    conn.execute("SELECT 1")
```

## Using contextlib (Simpler)
```python
from contextlib import contextmanager

@contextmanager
def timer(label):
    start = time.time()
    yield
    print(f"{label}: {time.time() - start:.2f}s")

with timer("DB query"):
    result = db.query("SELECT * FROM users")
```

## Useful Built-in Context Managers
```python
# Temporarily change directory
import os
from contextlib import chdir  # Python 3.11+
with chdir("/tmp"):
    print(os.getcwd())  # /tmp

# Suppress specific exceptions
from contextlib import suppress
with suppress(FileNotFoundError):
    os.remove("maybe_exists.txt")

# Redirect stdout
from contextlib import redirect_stdout
with redirect_stdout(open("output.txt", "w")):
    print("This goes to file")
```

## async Context Managers
```python
async with aiohttp.ClientSession() as session:
    async with session.get(url) as response:
        data = await response.json()
```
