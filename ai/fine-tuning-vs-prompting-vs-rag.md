# Fine-Tuning vs Prompting vs RAG — When to Use What

## Decision Framework

```
Do you need the model to USE external/changing data?
├─ Yes → RAG
│   (Company docs, real-time data, user-specific info)
└─ No
    ├─ Do you need a specific OUTPUT STYLE or FORMAT?
    │  ├─ Yes → Fine-Tuning
    │  │   (Consistent tone, domain jargon, structured output)
    │  └─ No
    │      └─ Prompt Engineering (start here, always)
    └─ Do you need domain-specific KNOWLEDGE?
       ├─ Static knowledge → Fine-Tuning
       └─ Dynamic/changing → RAG
```

## Comparison

| Aspect | Prompt Engineering | RAG | Fine-Tuning |
|--------|-------------------|-----|-------------|
| **Cost** | Lowest | Medium | Highest |
| **Speed to deploy** | Minutes | Days | Weeks |
| **Data needed** | 0 | Documents | 100-10K+ examples |
| **Knowledge updates** | Change prompt | Re-index docs | Retrain model |
| **Hallucination** | Higher | Lower (grounded) | Medium |
| **Customization** | Limited | Medium | High |
| **Best for** | General tasks | Knowledge Q&A | Style/format |

## When to Use Each

### Prompt Engineering (Always Start Here)
```
✅ The task is well-defined
✅ Few-shot examples fit in context
✅ You need quick iteration
✅ General knowledge is sufficient

Example: "You are a senior DevOps engineer. Explain {concept}
         in 3 bullet points with practical examples."
```

### RAG (Most Common for Enterprise)
```
✅ Need to query company/private data
✅ Data changes frequently
✅ Need source attribution/citations
✅ Can't fit all data in the prompt
✅ Need to reduce hallucinations

Example: Internal documentation chatbot, legal document search,
         customer support with product knowledge base
```

### Fine-Tuning (When RAG + Prompts Aren't Enough)
```
✅ Need consistent output format/style
✅ Need to learn domain-specific jargon
✅ Need to reduce token usage (shorter prompts)
✅ Need specialized behavior (e.g., SQL generation)
✅ Have labeled training data

Example: Medical report summarization in a specific format,
         code completion for internal framework,
         customer email tone matching
```

## Fine-Tuning Methods

### Full Fine-Tuning
```
Train ALL parameters of the model.
Cost: Very high (needs many GPUs)
Data: 10K+ examples
Used by: Companies training their own models
```

### LoRA (Low-Rank Adaptation)
```
Freeze the base model, train small adapter matrices.

Base Model (frozen): [████████████████] 7B parameters
LoRA adapters:       [██]               ~1-10M parameters

Benefits:
  - 10-100x less compute
  - Multiple adapters for different tasks
  - Can merge back into base model
  - Works with consumer GPUs
```

### QLoRA (Quantized LoRA)
```
Quantize base model to 4-bit + LoRA adapters.
Train a 70B model on a single 48GB GPU!

Normal: 70B × 16-bit = 140 GB VRAM
QLoRA:  70B × 4-bit + LoRA = ~35 GB VRAM
```

## Combining Approaches (Best Practice)

```
Production system:
  1. Fine-tune for style/format (how to answer)
  2. RAG for knowledge (what to answer)
  3. Prompt engineering for instructions (what to do)

Example: Customer support bot
  Fine-tuned: Match company's tone and response format
  RAG: Retrieve product docs, order history, FAQ
  Prompt: "Answer the customer's question using the context.
           Be empathetic. If unsure, escalate to human."
```

## Interview Answer

> "I always start with prompt engineering — it's the fastest and cheapest to iterate. When the model needs access to private or changing data, I add RAG to ground responses in real documents. Fine-tuning is my last resort, reserved for when I need a specific output style or format that prompting can't achieve consistently. In practice, I combine all three: fine-tune a small model (LoRA) for consistent response formatting, RAG for knowledge retrieval, and prompt engineering for task instructions. The key insight is that RAG handles 'what to say' (knowledge) while fine-tuning handles 'how to say it' (style)."
