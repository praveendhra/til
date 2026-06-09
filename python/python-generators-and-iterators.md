# Python Generators and Iterators

## Generator Basics

Generators produce values lazily — one at a time, on demand. Memory efficient for large datasets.

```python
def fibonacci():
    a, b = 0, 1
    while True:
        yield a
        a, b = b, a + b

# Only computes values as needed
fib = fibonacci()
first_10 = [next(fib) for _ in range(10)]
```

## Generator vs List Comprehension

```python
# List — all in memory at once
squares_list = [x**2 for x in range(10_000_000)]  # ~80MB

# Generator — one value at a time
squares_gen = (x**2 for x in range(10_000_000))   # ~120 bytes
```

## yield from — Delegating to Sub-generators

```python
def flatten(nested):
    for item in nested:
        if isinstance(item, list):
            yield from flatten(item)  # delegate recursively
        else:
            yield item

list(flatten([1, [2, [3, 4]], 5]))  # [1, 2, 3, 4, 5]
```

## Generator as Coroutine (send values in)

```python
def running_average():
    total = 0
    count = 0
    average = None
    while True:
        value = yield average
        total += value
        count += 1
        average = total / count

avg = running_average()
next(avg)          # prime the generator
avg.send(10)       # 10.0
avg.send(20)       # 15.0
avg.send(30)       # 20.0
```

## Practical Patterns

```python
# Reading large files line by line
def read_large_file(path):
    with open(path) as f:
        for line in f:
            yield line.strip()

# Chaining generators (pipeline)
def parse_logs(lines):
    for line in lines:
        yield json.loads(line)

def filter_errors(records):
    for r in records:
        if r["level"] == "ERROR":
            yield r

# Compose the pipeline — nothing executes until iteration
lines = read_large_file("app.log")
records = parse_logs(lines)
errors = filter_errors(records)

for error in errors:  # only now does processing begin
    alert(error)
```

## itertools Highlights

```python
from itertools import islice, chain, groupby, batched

# Take first N from infinite generator
islice(fibonacci(), 10)

# Combine multiple iterables
chain([1, 2], [3, 4])  # 1, 2, 3, 4

# Group consecutive items
data = sorted(users, key=lambda u: u["dept"])
for dept, group in groupby(data, key=lambda u: u["dept"]):
    print(dept, list(group))

# Batch into chunks (Python 3.12+)
list(batched(range(10), 3))  # [(0,1,2), (3,4,5), (6,7,8), (9,)]
```

## Key Takeaway

Use generators when you need to process data that doesn't fit in memory, or when you want to build composable processing pipelines. They're Python's answer to streaming.
