# Python Decorators and Closures

## How Decorators Work

A decorator is just a function that takes a function and returns a modified function.

```python
import functools
import time

def timer(func):
    @functools.wraps(func)  # preserves __name__, __doc__
    def wrapper(*args, **kwargs):
        start = time.perf_counter()
        result = func(*args, **kwargs)
        elapsed = time.perf_counter() - start
        print(f"{func.__name__} took {elapsed:.4f}s")
        return result
    return wrapper

@timer
def slow_function():
    time.sleep(1)
```

## Decorators with Arguments

Need an extra layer of nesting — a decorator factory:

```python
def retry(max_attempts=3, delay=1):
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            for attempt in range(max_attempts):
                try:
                    return func(*args, **kwargs)
                except Exception as e:
                    if attempt == max_attempts - 1:
                        raise
                    time.sleep(delay)
        return wrapper
    return decorator

@retry(max_attempts=5, delay=2)
def call_flaky_api():
    ...
```

## Class-Based Decorators

Useful when you need to maintain state:

```python
class CountCalls:
    def __init__(self, func):
        functools.update_wrapper(self, func)
        self.func = func
        self.count = 0

    def __call__(self, *args, **kwargs):
        self.count += 1
        print(f"{self.func.__name__} called {self.count} times")
        return self.func(*args, **kwargs)
```

## Stacking Decorators

Applied bottom-up:

```python
@timer          # 2nd: wraps the retrying version
@retry(max_attempts=3)  # 1st: wraps original
def fetch_data():
    ...
```

## Closures — The Foundation

A closure captures variables from the enclosing scope:

```python
def make_multiplier(factor):
    def multiply(n):
        return n * factor  # 'factor' is captured
    return multiply

double = make_multiplier(2)
double(5)  # 10
```

## Practical Patterns

- **Caching**: `@functools.lru_cache(maxsize=128)` — built-in memoization
- **Auth checks**: Verify permissions before executing view functions
- **Logging**: Automatically log function entry/exit
- **Validation**: Type-check or validate arguments before calling

## Key Gotcha

Always use `@functools.wraps(func)` — without it, the decorated function loses its original name and docstring, breaking introspection and debugging.
