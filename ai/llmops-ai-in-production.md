# LLMOps — Running AI in Production

## The LLMOps Stack

```
┌────────────────────────────────────────────┐
│           Application Layer                │
│  (Chat UI, API endpoints, agents)          │
├────────────────────────────────────────────┤
│           Orchestration Layer              │
│  (LangChain, LangGraph, prompt management) │
├────────────────────────────────────────────┤
│           Model Layer                      │
│  (OpenAI API, self-hosted LLaMA, vLLM)     │
├────────────────────────────────────────────┤
│           Data Layer                       │
│  (Vector DB, document store, cache)        │
├────────────────────────────────────────────┤
│           Observability Layer              │
│  (LangSmith, Langfuse, tracing, metrics)   │
└────────────────────────────────────────────┘
```

## Key Production Concerns

### 1. Latency
```
Typical LLM latency:
  Time to first token (TTFT): 200-500ms
  Token generation: 30-80 tokens/sec
  Total for 500-token response: 1-3 seconds

Optimization:
  - Streaming responses (show tokens as generated)
  - Prompt caching (reuse common prefixes)
  - Smaller/faster models for simple tasks
  - Response caching for repeated queries
  - Edge deployment for lower network latency
```

### 2. Cost Management
```python
# Track cost per request
class CostTracker:
    PRICES = {
        "gpt-4": {"input": 2.50, "output": 10.00},   # per 1M tokens
        "gpt-4o-mini": {"input": 0.15, "output": 0.60},
        "claude-3-haiku": {"input": 0.25, "output": 1.25},
    }

    def calculate_cost(self, model, input_tokens, output_tokens):
        prices = self.PRICES[model]
        return (input_tokens * prices["input"] +
                output_tokens * prices["output"]) / 1_000_000

# Cost reduction strategies:
# 1. Route simple queries to cheaper models
# 2. Cache responses for common queries
# 3. Shorten prompts (remove unnecessary context)
# 4. Set max_tokens limits
# 5. Use batch API for non-real-time tasks (50% discount)
```

### 3. Model Router (Cost/Quality Optimization)
```python
def route_query(query: str, complexity: str) -> str:
    if complexity == "simple":
        # FAQ, simple questions → cheap model
        return "gpt-4o-mini"      # $0.15/1M input
    elif complexity == "medium":
        # General tasks → balanced model
        return "gpt-4o"           # $2.50/1M input
    else:
        # Complex reasoning, code → best model
        return "gpt-4"            # $2.50/1M input

# Use a small classifier to determine complexity automatically
```

### 4. Caching
```python
import hashlib
from redis import Redis

redis = Redis()

def cached_llm_call(prompt: str, model: str) -> str:
    # Create cache key from prompt + model
    cache_key = hashlib.sha256(f"{model}:{prompt}".encode()).hexdigest()

    # Check cache
    cached = redis.get(cache_key)
    if cached:
        return cached.decode()

    # Call LLM
    response = llm.invoke(prompt)

    # Cache with TTL (1 hour for dynamic, 24h for static)
    redis.setex(cache_key, 3600, response)
    return response
```

### 5. Rate Limiting & Quotas
```
Per-user limits:
  Free tier: 10 queries/hour, 1K tokens/query
  Pro tier: 100 queries/hour, 4K tokens/query
  Enterprise: Custom limits

Implementation:
  - Token bucket per user (Redis)
  - Queue for burst management
  - Graceful degradation (cheaper model on quota)
```

### 6. Fallback & Reliability
```python
async def reliable_llm_call(prompt: str) -> str:
    providers = [
        ("openai", "gpt-4"),
        ("anthropic", "claude-3-sonnet"),
        ("google", "gemini-1.5-pro"),
    ]

    for provider, model in providers:
        try:
            return await call_provider(provider, model, prompt)
        except (RateLimitError, TimeoutError, ServiceUnavailableError):
            continue

    raise AllProvidersFailedError("No LLM provider available")
```

## Deployment Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Client    │────→│   API GW     │────→│  App Server  │
│   (React)   │     │  (Kong/Nginx)│     │  (FastAPI)   │
└─────────────┘     │  - Rate limit│     │  - Routing   │
                    │  - Auth      │     │  - Cache     │
                    └──────────────┘     │  - Guardrails│
                                        └──────┬──────┘
                                               │
                          ┌────────────────────┤
                          ↓                    ↓
                    ┌───────────┐        ┌───────────┐
                    │ Vector DB │        │ LLM API   │
                    │ (Qdrant)  │        │ (OpenAI)  │
                    └───────────┘        └───────────┘
```

## Monitoring Dashboard Metrics

```
Business metrics:
  - Queries per day/hour
  - User satisfaction (thumbs up/down ratio)
  - Task completion rate

Technical metrics:
  - Latency (p50, p95, p99)
  - Token usage (input, output)
  - Cost per query, per user, per day
  - Error rate by type (rate limit, timeout, content filter)
  - Cache hit rate

Quality metrics:
  - Hallucination rate (sampled)
  - Retrieval recall (for RAG)
  - Guardrail trigger rate
```

## Interview Answer

> "For LLM applications in production, I focus on five areas: latency (streaming responses, prompt caching), cost management (model routing — cheap models for simple queries, GPT-4 for complex ones, Redis caching for repeated queries), reliability (multi-provider fallback, circuit breakers), safety (input/output guardrails, rate limiting per user), and observability (LangSmith for tracing, token usage dashboards, sampled quality evaluation). The architecture is a FastAPI service behind an API gateway with Redis for caching and rate limiting, connecting to both a vector database for RAG and multiple LLM providers for redundancy."
