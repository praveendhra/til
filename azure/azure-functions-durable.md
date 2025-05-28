# Azure Durable Functions

## What Are They?
Extension of Azure Functions that lets you write stateful workflows in serverless.

## Patterns

### 1. Function Chaining
```python
def orchestrator(context):
    result1 = yield context.call_activity("StepA", input_data)
    result2 = yield context.call_activity("StepB", result1)
    result3 = yield context.call_activity("StepC", result2)
    return result3
```

### 2. Fan-Out / Fan-In
```python
def orchestrator(context):
    tasks = []
    for item in work_items:
        tasks.append(context.call_activity("ProcessItem", item))
    results = yield context.task_all(tasks)
    yield context.call_activity("Aggregate", results)
```

### 3. Human Interaction (Approval)
```python
def orchestrator(context):
    yield context.call_activity("SendApprovalEmail", request)
    approval = yield context.wait_for_external_event("ApprovalEvent")
    if approval["approved"]:
        yield context.call_activity("ProcessRequest", request)
```

### 4. Monitor Pattern
```python
def orchestrator(context):
    while True:
        status = yield context.call_activity("CheckStatus", job_id)
        if status == "completed":
            return status
        yield context.create_timer(context.current_utc_datetime + timedelta(minutes=5))
```

## Key Benefits
- Automatic checkpointing and replay
- Handles long-running workflows (days/weeks)
- Built-in retry and error handling
