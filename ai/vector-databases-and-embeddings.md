# Vector Databases & Embeddings — Semantic Search at Scale

## What Are Embeddings?

Dense numerical representations of text that capture meaning.

```
"king"  → [0.2, 0.8, 0.1, 0.5, ...]   (1536 dimensions)
"queen" → [0.2, 0.8, 0.1, 0.6, ...]   (very similar!)
"car"   → [0.9, 0.1, 0.7, 0.2, ...]   (very different)

Famous example:
  king - man + woman ≈ queen
  (vector arithmetic captures semantic relationships)
```

## How Similarity Search Works

```
Cosine Similarity: cos(θ) = (A·B) / (||A|| × ||B||)
  Range: -1 to 1 (1 = identical, 0 = unrelated, -1 = opposite)

Query: "How to deploy to Kubernetes?"
  ↓ embed
Query vector: [0.3, 0.7, ...]

Compare against all stored vectors:
  "K8s deployment guide"     → similarity: 0.92 ✓ (match!)
  "Docker container basics"  → similarity: 0.71
  "Python decorators"        → similarity: 0.15 ✗
```

## Indexing Algorithms (How Vector DBs Are Fast)

### Brute Force (Exact)
```
Compare query against ALL vectors.
O(n × d) — too slow for millions of vectors.
100M vectors × 1536 dims = ~20 seconds per query
```

### HNSW (Hierarchical Navigable Small World)
```
Most popular algorithm. Used by: Qdrant, Weaviate, pgvector, Pinecone.

Builds a multi-layer graph:
Layer 3: [A] --------- [D]          (few nodes, long-range links)
Layer 2: [A] --- [C] -- [D]
Layer 1: [A]-[B]-[C]-[D]-[E]       (all nodes, short-range links)
Layer 0: [A][B][C][D][E][F][G][H]   (all nodes, fine-grained)

Search: Start at top layer, navigate down to find nearest neighbors.
  Build time: O(n log n)
  Query time: O(log n)
  Memory: High (stores graph structure)
  Accuracy: ~95-99% recall
```

### IVF (Inverted File Index)
```
Cluster vectors into buckets, only search relevant buckets.
Used by: FAISS

1. Cluster into K centroids (K-Means)
2. Assign each vector to nearest centroid
3. At query time, find nearest centroids, search only those

  [Cluster 1: vectors about animals]
  [Cluster 2: vectors about technology]  ← Search here!
  [Cluster 3: vectors about food]

Query time: O(n/K × nprobe)
  nprobe = number of clusters to search (trade-off: speed vs recall)
```

## Vector Database Comparison

| Feature | Pinecone | Qdrant | Weaviate | pgvector | ChromaDB |
|---------|----------|--------|----------|----------|----------|
| **Hosting** | Managed only | Both | Both | Self + managed | Embedded |
| **Scale** | Billions | Billions | Billions | Millions | Thousands |
| **Filtering** | Metadata | Advanced | GraphQL | SQL | Basic |
| **Hybrid search** | ✅ | ✅ | ✅ | ✅ (0.7+) | ❌ |
| **Multi-tenancy** | Namespaces | Collections | Tenants | Schemas | Collections |
| **Best for** | Production SaaS | Performance | Hybrid search | Postgres users | Prototyping |

## Practical Tips

### Embedding Best Practices
```
1. Use the SAME model for indexing and querying
   (mixing models = garbage results)

2. Normalize embeddings for cosine similarity
   (most APIs do this automatically)

3. Prefix queries for asymmetric models:
   Index: "Kubernetes pods are the smallest deployable units"
   Query: "query: What are Kubernetes pods?"

4. Batch embedding calls:
   ❌ 1000 separate API calls (slow, expensive)
   ✅ 1 batch call with 1000 texts
```

### Metadata Filtering
```python
# Pinecone example
index.query(
    vector=query_embedding,
    top_k=10,
    filter={
        "category": {"$eq": "devops"},
        "date": {"$gte": "2024-01-01"},
        "source": {"$in": ["docs", "wiki"]}
    }
)
# First filter by metadata, THEN vector search
# Much faster than searching everything
```

## Interview Answer

> "Embeddings convert text into dense vectors that capture semantic meaning, enabling similarity search. Vector databases use approximate nearest neighbor algorithms like HNSW to search millions of vectors in milliseconds. For production RAG, I use Qdrant or Pinecone with metadata filtering to scope searches (by user, document type, date). The key decisions are: embedding model (I prefer OpenAI's text-embedding-3-small for cost/quality balance), chunk size (500 tokens), and index type (HNSW for recall, IVF for memory efficiency). I always use the same embedding model for indexing and querying, and implement hybrid search combining dense embeddings with sparse BM25 for better recall."
