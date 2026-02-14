# RAG — Retrieval-Augmented Generation

## The Problem RAG Solves

LLMs have two major limitations:
1. **Knowledge cutoff**: Training data has a date limit
2. **Hallucination**: Makes up facts confidently

RAG fixes both by retrieving real documents before generating a response.

## How RAG Works

```
User Question: "What's our refund policy for enterprise customers?"

Step 1: RETRIEVE
  Query → Embedding → Vector Search → Top-K relevant documents
  [policy_v3.pdf, enterprise_terms.doc, faq.md]

Step 2: AUGMENT
  System: "Answer based ONLY on the following context:"
  Context: [retrieved documents]
  Question: "What's our refund policy for enterprise customers?"

Step 3: GENERATE
  LLM generates answer grounded in the retrieved documents
  → "Enterprise customers can request a full refund within 30 days..."
```

## Architecture

```
┌──────────────────────────────────────────────┐
│              Ingestion Pipeline               │
│                                              │
│  Documents → Chunking → Embedding → Vector DB│
│  (PDF,MD,    (500-1000   (OpenAI,   (Pinecone│
│   HTML...)    tokens)    Cohere)    Qdrant,   │
│                                    Weaviate)  │
└──────────────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│              Query Pipeline                   │
│                                              │
│  User Query → Embed Query → Vector Search    │
│            → Retrieve Top-K chunks           │
│            → Build prompt with context       │
│            → LLM generates answer            │
│            → (Optional) Citation/source      │
└──────────────────────────────────────────────┘
```

## Chunking Strategies

```
1. Fixed-size chunks (simple but can split mid-sentence)
   chunk_size=500, chunk_overlap=50

2. Recursive text splitting (better — respects structure)
   Split by: \n\n → \n → sentences → words
   Keeps paragraphs together when possible

3. Semantic chunking (best — uses embeddings)
   Group sentences with similar meaning together
   More coherent chunks, better retrieval

4. Document-aware chunking
   Respect headers, sections, tables
   Keep related information together
```

### Chunk Size Trade-offs
```
Small chunks (200 tokens):
  ✅ More precise retrieval
  ❌ Loses context, more chunks to search
  → Good for: Q&A, specific facts

Large chunks (1000 tokens):
  ✅ More context preserved
  ❌ Diluted relevance, slower
  → Good for: Summarization, complex topics

Sweet spot: 500-800 tokens with 10-20% overlap
```

## Embedding Models

| Model | Dimensions | Quality | Speed | Cost |
|-------|-----------|---------|-------|------|
| OpenAI text-embedding-3-small | 1536 | Good | Fast | $0.02/1M tokens |
| OpenAI text-embedding-3-large | 3072 | Best | Medium | $0.13/1M tokens |
| Cohere embed-v3 | 1024 | Great | Fast | $0.10/1M tokens |
| sentence-transformers (local) | 384-768 | Good | Variable | Free |
| BGE-large-en-v1.5 (local) | 1024 | Great | Medium | Free |

## Vector Databases

| Database | Type | Best For |
|----------|------|----------|
| **Pinecone** | Managed | Production, zero-ops |
| **Qdrant** | Self-hosted/Cloud | Performance, filtering |
| **Weaviate** | Self-hosted/Cloud | Hybrid search |
| **ChromaDB** | Embedded | Prototyping, local dev |
| **pgvector** | PostgreSQL extension | Already using Postgres |
| **FAISS** | Library (Meta) | Research, local search |

## Advanced RAG Techniques

### 1. Hybrid Search (Dense + Sparse)
```
Dense search (embeddings): Good for semantic similarity
  "refund policy" matches "return merchandise authorization"

Sparse search (BM25/keywords): Good for exact terms
  "RMA-2024-001" matches exact document ID

Hybrid = weighted combination of both
  score = α × dense_score + (1-α) × sparse_score
```

### 2. Re-ranking
```
Step 1: Retrieve top-50 with fast vector search
Step 2: Re-rank with a cross-encoder (more accurate but slower)
Step 3: Take top-5 for the LLM

Models: Cohere Rerank, cross-encoder/ms-marco-MiniLM
```

### 3. Query Transformation
```
Original: "Why is my K8s pod crashing?"

HyDE (Hypothetical Document Embedding):
  Generate a hypothetical answer, embed that instead
  → Better matches against actual documentation

Multi-query:
  Rephrase into multiple queries for broader retrieval
  → "kubernetes pod CrashLoopBackOff"
  → "container restart reasons"
  → "pod failing health checks"
```

### 4. Contextual Compression
```
Retrieved chunk (500 tokens): [Long document about K8s pods...]
  ↓ LLM compression
Compressed (50 tokens): "Pods crash due to OOM, missing images,
  or failed health checks. Check: kubectl describe pod <name>"
```

## Evaluation Metrics

```
Retrieval quality:
  - Recall@K: What % of relevant docs are in top-K results?
  - MRR: How high is the first relevant result?
  - NDCG: Are relevant results ranked properly?

Generation quality:
  - Faithfulness: Does the answer match the context? (no hallucination)
  - Relevance: Does the answer address the question?
  - Context precision: Are retrieved chunks actually useful?

Tools: RAGAS framework, LangSmith, TruLens
```

## Production Considerations

```
1. Monitoring: Track retrieval quality, latency, cost
2. Caching: Cache embeddings and frequent queries
3. Freshness: Re-index documents when they change
4. Access control: Filter results by user permissions
5. Fallback: What to say when no relevant docs are found
6. Citations: Show source documents for trust/verification
```

## Interview Answer

> "RAG grounds LLM responses in real data by retrieving relevant documents before generation. The pipeline is: chunk documents → embed → store in vector DB → at query time, embed the question → retrieve top-K chunks → feed to LLM as context. I use recursive text splitting with 500-token chunks and 10% overlap. For production, I combine dense (embedding) and sparse (BM25) search for hybrid retrieval, then re-rank with a cross-encoder for precision. The key metrics are retrieval recall and generation faithfulness — I use RAGAS for automated evaluation. Common pitfalls are chunks that are too large (diluted relevance) or missing re-ranking (relevant docs ranked too low)."
