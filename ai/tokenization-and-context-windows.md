# Tokenization & Context Windows — The Constraints of LLMs

## What Is Tokenization?

LLMs don't process text character-by-character. They split text into **tokens** (subword units).

```
Text: "Kubernetes deployment failed"
Tokens: ["Kub", "ernetes", " deployment", " failed"]  (4 tokens)

Text: "The cat sat on the mat"
Tokens: ["The", " cat", " sat", " on", " the", " mat"]  (6 tokens)

Text: "你好世界"
Tokens: ["你", "好", "世", "界"]  (4 tokens — non-English is less efficient)

Rule of thumb (English):
  1 token ≈ 4 characters ≈ 0.75 words
  100 tokens ≈ 75 words
  1 page of text ≈ ~500 tokens
```

## BPE (Byte Pair Encoding)

The most common tokenization algorithm (used by GPT, LLaMA):

```
Training:
  Start with character-level vocabulary: {a, b, c, ..., z, space, ...}
  Repeatedly merge most frequent pair:
    "th" appears most → merge into "th"
    "the" appears most → merge into "the"
    "ing" appears most → merge into "ing"
  Continue until vocabulary reaches target size (32K-100K tokens)

Result: Common words are single tokens, rare words are split:
  "the" → ["the"]        (1 token — very common)
  "Kubernetes" → ["Kub", "ernetes"]  (2 tokens — less common)
  "xylophone" → ["xy", "lo", "phone"] (3 tokens — rare)
```

## Context Window

The maximum number of tokens the model can process at once (input + output).

```
Model context windows:
  GPT-3.5:    4K / 16K tokens
  GPT-4:      8K / 32K / 128K tokens
  Claude 3.5: 200K tokens
  Gemini 1.5: 1M+ tokens
  LLaMA 3:    8K / 128K tokens

What fits in context:
  4K tokens ≈ ~6 pages of text
  32K tokens ≈ ~50 pages
  128K tokens ≈ ~200 pages (~a short book)
  200K tokens ≈ ~300 pages
  1M tokens ≈ ~1500 pages (~War and Peace)
```

## Managing Context Limits

```
Problem: User has a 500-page document but context is 128K tokens.

Solutions:
1. Chunking + RAG: Search for relevant chunks, only include those
2. Summarization: Summarize document, use summary as context
3. Map-Reduce: Process chunks in parallel, combine results
4. Sliding window: Process overlapping windows sequentially
5. Use a model with larger context (Gemini 1.5 Pro = 1M tokens)
```

### Map-Reduce Pattern
```
Document: [Chunk 1] [Chunk 2] [Chunk 3] ... [Chunk N]
              ↓          ↓          ↓              ↓
Map:    [Summary 1] [Summary 2] [Summary 3] ... [Summary N]
              ↓          ↓          ↓              ↓
Reduce:         [Combined Final Summary]
```

## Cost Implications

```
OpenAI GPT-4 pricing (per 1M tokens):
  Input:  $2.50   (reading your prompt)
  Output: $10.00  (generating response)

128K context window fully used:
  Input cost:  128K × $2.50/1M = $0.32 per query
  With output: + 4K output × $10/1M = $0.04
  Total: ~$0.36 per query

At 1000 queries/day: $360/day = $10,800/month!

Optimization:
  - Use shorter prompts
  - Cache common context
  - Use cheaper models for simple tasks
  - Reduce retrieved chunks in RAG
```

## Interview Answer

> "Tokenization splits text into subword units using BPE — common words are single tokens while rare words are split. Context windows limit total input+output tokens: GPT-4 supports 128K tokens (~200 pages). For documents exceeding context limits, I use RAG to retrieve only relevant chunks, or map-reduce to process chunks in parallel and combine summaries. Cost management is critical — a full 128K context query costs ~$0.36, so I optimize by caching context, using smaller models for simple tasks, and minimizing retrieved chunks. Non-English text is less token-efficient, which matters for multilingual applications."
