# LLM Evaluation & Testing — Measuring What Matters

## Why Evaluation Is Hard

```
Traditional software: assert add(2, 3) == 5 → deterministic
LLM output: "The sum of 2 and 3 is 5" → correct but varies each time
            "2 + 3 equals 5, which is a prime number" → also correct + bonus info
            "Five" → correct but different format
```

## Evaluation Approaches

### 1. Automated Metrics

```
BLEU (translation): N-gram overlap with reference
  Limitation: "The dog bit the man" scores same as "The man bit the dog"

ROUGE (summarization): Recall of reference n-grams
  ROUGE-1: Unigram overlap
  ROUGE-L: Longest common subsequence

BERTScore: Semantic similarity using BERT embeddings
  Better than BLEU/ROUGE for meaning

Exact Match: For structured outputs (JSON, SQL)
  result == expected_output
```

### 2. LLM-as-Judge

Use a strong model to evaluate a weaker model's output:

```python
evaluation_prompt = """
Rate the following response on a scale of 1-5 for:
1. Accuracy: Is the information correct?
2. Completeness: Does it address all parts of the question?
3. Clarity: Is it well-written and easy to understand?

Question: {question}
Response: {response}
Reference: {reference_answer}

Provide scores and brief justification for each.
"""
```

**G-Eval Framework**:
```
1. Define criteria (accuracy, relevance, fluency)
2. Generate evaluation steps with CoT
3. Score each criterion 1-5
4. Aggregate scores

Pro: Correlates well with human judgment
Con: Evaluator model has its own biases
```

### 3. RAG-Specific Evaluation (RAGAS)

```python
from ragas import evaluate
from ragas.metrics import (
    faithfulness,        # Is answer supported by context?
    answer_relevancy,    # Does answer address the question?
    context_precision,   # Are retrieved chunks relevant?
    context_recall       # Are all needed chunks retrieved?
)

result = evaluate(
    dataset,
    metrics=[faithfulness, answer_relevancy,
             context_precision, context_recall]
)
# faithfulness: 0.92 (good — low hallucination)
# context_precision: 0.85 (most retrieved chunks are useful)
```

### 4. Human Evaluation

```
Gold standard but expensive and slow.

Methods:
  - A/B testing (which response is better?)
  - Likert scale (rate 1-5)
  - Task completion rate
  - Time to find information

When to use:
  - Final validation before launch
  - Calibrating automated metrics
  - Edge cases and safety evaluation
```

## Testing in CI/CD

```python
# pytest tests for LLM applications
import pytest

class TestRAGPipeline:
    def test_retrieval_relevance(self):
        """Top-5 retrieved chunks should contain the answer."""
        query = "What is our refund policy?"
        chunks = retriever.get_relevant_documents(query)
        assert any("refund" in c.page_content.lower() for c in chunks)

    def test_answer_contains_key_info(self):
        """Response must mention the 30-day window."""
        answer = rag_chain.invoke("What is our refund policy?")
        assert "30 day" in answer.lower() or "thirty day" in answer.lower()

    def test_no_hallucination(self):
        """Response should not contain info not in the context."""
        answer = rag_chain.invoke("What is our CEO's phone number?")
        assert "I don't" in answer or "not available" in answer.lower()

    def test_response_format(self):
        """Structured output should be valid JSON."""
        result = extraction_chain.invoke("John Smith, 35, Boston")
        data = json.loads(result)
        assert "name" in data
        assert "age" in data
```

## Observability & Monitoring

```
Production LLM metrics:
  - Latency (p50, p95, p99)
  - Token usage (input + output)
  - Cost per query
  - Error rate
  - Retrieval recall (for RAG)
  - User feedback (thumbs up/down)
  - Hallucination rate (sampled evaluation)

Tools: LangSmith, Langfuse, Helicone, Weights & Biases
```

## Interview Answer

> "I evaluate LLM applications at three levels: retrieval quality (using recall@K and MRR for RAG), generation quality (using LLM-as-judge with defined rubrics and RAGAS metrics for faithfulness), and end-to-end (human evaluation and A/B testing). In CI/CD, I run automated tests that check retrieval relevance, response format, key information presence, and hallucination boundaries. For production monitoring, I track latency, token costs, user feedback, and sample outputs for periodic LLM-as-judge evaluation. The key insight is that deterministic tests (format, key phrases) catch regressions, while statistical tests (RAGAS scores) measure quality trends."
