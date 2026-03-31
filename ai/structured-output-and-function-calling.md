# Structured Output & Function Calling — Reliable LLM Integration

## The Problem

LLMs output free-form text. Applications need structured data.

```
❌ Free-form: "The user's name is John, he's 35 years old..."
✅ Structured: {"name": "John", "age": 35}
```

## Structured Output Approaches

### 1. JSON Mode
```python
response = client.chat.completions.create(
    model="gpt-4o",
    response_format={"type": "json_object"},
    messages=[
        {"role": "system", "content": "Output valid JSON only."},
        {"role": "user", "content": "Extract: John Smith, 35, engineer"}
    ]
)
# Guaranteed valid JSON (but schema not enforced)
```

### 2. Structured Outputs (Schema Enforcement)
```python
from pydantic import BaseModel

class UserProfile(BaseModel):
    name: str
    age: int
    occupation: str
    skills: list[str]

response = client.beta.chat.completions.parse(
    model="gpt-4o",
    response_format=UserProfile,
    messages=[
        {"role": "user",
         "content": "Extract: John Smith, 35, DevOps engineer, "
                    "skilled in K8s, Terraform, AWS"}
    ]
)

user = response.choices[0].message.parsed
# UserProfile(name="John Smith", age=35, occupation="DevOps engineer",
#             skills=["Kubernetes", "Terraform", "AWS"])
```

### 3. Tool/Function Calling
```python
tools = [{
    "type": "function",
    "function": {
        "name": "create_ticket",
        "description": "Create a support ticket",
        "parameters": {
            "type": "object",
            "properties": {
                "title": {"type": "string"},
                "priority": {"type": "string", "enum": ["low", "medium", "high"]},
                "description": {"type": "string"}
            },
            "required": ["title", "priority"]
        }
    }
}]

response = client.chat.completions.create(
    model="gpt-4o",
    messages=[{"role": "user", "content": "Create a high priority ticket: DB is down"}],
    tools=tools
)

# Model returns structured tool call:
# {"name": "create_ticket", "arguments": {
#     "title": "Database Down",
#     "priority": "high",
#     "description": "Production database is unresponsive"
# }}
```

### 4. Instructor Library (Pydantic + Retry)
```python
import instructor
from openai import OpenAI
from pydantic import BaseModel, Field

client = instructor.from_openai(OpenAI())

class ExtractedInfo(BaseModel):
    name: str
    age: int = Field(ge=0, le=150)  # Validation!
    email: str = Field(pattern=r'^[\w.-]+@[\w.-]+\.\w+$')

# Automatically retries if validation fails
result = client.chat.completions.create(
    model="gpt-4o",
    response_model=ExtractedInfo,
    messages=[{"role": "user", "content": "John Smith, 35, john@acme.com"}],
    max_retries=3  # Retry with validation errors fed back to model
)
```

## Parallel Function Calling

```python
# Model can call multiple functions at once
response = client.chat.completions.create(
    model="gpt-4o",
    messages=[{"role": "user",
               "content": "What's the weather in Austin AND the stock price of AAPL?"}],
    tools=[weather_tool, stock_tool]
)

# Returns multiple tool calls:
# [
#   {"name": "get_weather", "arguments": {"location": "Austin, TX"}},
#   {"name": "get_stock_price", "arguments": {"symbol": "AAPL"}}
# ]
# Execute both in parallel, return results to model
```

## Best Practices

```
1. Always validate output (Pydantic models)
2. Use enums for constrained fields (priority, status)
3. Provide clear descriptions for each field
4. Set reasonable defaults
5. Use max_retries for self-correction
6. Log raw responses for debugging
7. Handle refusals (model may refuse to extract certain info)
```

## Interview Answer

> "I use structured outputs to ensure LLM responses are machine-parseable. For simple extraction, JSON mode guarantees valid JSON. For strict schema enforcement, I use Pydantic models with OpenAI's structured output feature — the model's output is guaranteed to match the schema. For tool integration, function calling lets the model decide when and how to call external APIs with typed arguments. I use the Instructor library for production because it adds Pydantic validation with automatic retries — if the model's output fails validation, the error is fed back and it corrects itself. Parallel function calling is key for performance: when the model needs multiple pieces of data, it can call all tools simultaneously."
