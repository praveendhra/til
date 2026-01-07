# Transformer Architecture — The Foundation of Modern AI

## Why Transformers Won

Before transformers (2017), we used RNNs/LSTMs for sequences:
```
RNN: Process tokens one-by-one → slow, forgets long contexts
     "The cat that sat on the mat that was blue was happy"
     By the time RNN reaches "happy", it's forgotten "cat"

Transformer: Process ALL tokens in parallel → fast, full context
     Self-attention connects every token to every other token
     "happy" directly attends to "cat" regardless of distance
```

## The Architecture

```
Input: "The cat sat"
  ↓
[Token Embeddings] + [Positional Encoding]
  ↓
┌─────────────────────────────────┐
│     Encoder Block (×N)          │
│  ┌───────────────────────────┐  │
│  │ Multi-Head Self-Attention │  │
│  │ + Residual Connection     │  │
│  │ + Layer Normalization     │  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ Feed-Forward Network      │  │
│  │ + Residual Connection     │  │
│  │ + Layer Normalization     │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
  ↓
Output Representation
```

## Self-Attention — The Key Innovation

For each token, compute: "How much should I attend to every other token?"

```
Input: "The cat sat on the mat"

Step 1: Create Q (Query), K (Key), V (Value) for each token
  Q = W_q × token_embedding
  K = W_k × token_embedding
  V = W_v × token_embedding

Step 2: Compute attention scores
  Score(cat, The) = Q_cat · K_The
  Score(cat, cat) = Q_cat · K_cat
  Score(cat, sat) = Q_cat · K_sat
  ... for all pairs

Step 3: Softmax → attention weights (sum to 1)
Step 4: Weighted sum of Values → context-aware representation

Attention(Q, K, V) = softmax(QK^T / √d_k) × V
```

The `√d_k` scaling prevents softmax from becoming too peaked (gradient vanishing).

## Multi-Head Attention

Run attention multiple times in parallel with different learned projections:
```
Head 1: Might learn syntax (subject-verb relationships)
Head 2: Might learn coreference (pronoun → noun)
Head 3: Might learn semantic similarity
...
Head 8-12: Different aspects of meaning

Final = Concat(Head_1, ..., Head_n) × W_o
```

## Positional Encoding

Transformers process tokens in parallel → no inherent position info.
Solution: Add position information to embeddings.

```
Original: sinusoidal functions (sin/cos at different frequencies)
Modern: Learned positional embeddings (GPT-2+)
Advanced: RoPE (Rotary Position Embedding) — used in LLaMA, GPT-NeoX
         ALiBi (Attention with Linear Biases) — used in BLOOM
```

## Encoder vs Decoder vs Encoder-Decoder

| Architecture | Models | Use Case |
|-------------|--------|----------|
| **Encoder-only** | BERT, RoBERTa | Classification, NER, embeddings |
| **Decoder-only** | GPT-4, LLaMA, Claude | Text generation, chat |
| **Encoder-Decoder** | T5, BART | Translation, summarization |

```
Encoder-only: Sees ALL tokens (bidirectional)
  → Good for understanding, not generation

Decoder-only: Sees only PAST tokens (causal/autoregressive)
  → Generates one token at a time, left-to-right
  → "The cat" → predicts "sat" → "The cat sat" → predicts "on" → ...

Encoder-Decoder: Encoder sees all input, decoder generates output
  → Input: "Translate to French: The cat sat"
  → Output: "Le chat s'est assis"
```

## Key Numbers to Know

| Model | Parameters | Context Window | Training Data |
|-------|-----------|---------------|---------------|
| GPT-3 | 175B | 4K tokens | 570GB text |
| GPT-4 | ~1.8T (MoE) | 128K tokens | ~13T tokens |
| LLaMA 3 | 8B / 70B | 128K tokens | 15T tokens |
| Claude 3.5 | Unknown | 200K tokens | Unknown |
| Gemini 1.5 | Unknown | 1M+ tokens | Unknown |

## Scaling Laws

```
Performance ∝ (compute)^0.05 ∝ (data)^0.095 ∝ (params)^0.076

More compute + more data + more parameters = better performance
But with diminishing returns (power law)

Chinchilla optimal: tokens ≈ 20 × parameters
  70B model needs ~1.4T training tokens
```

## Interview Answer

> "Transformers replaced RNNs by processing all tokens in parallel using self-attention, which computes a weighted relationship between every pair of tokens. The key formula is Attention(Q,K,V) = softmax(QK^T/√d_k)V. Multi-head attention runs this in parallel to capture different aspects of meaning. Modern LLMs like GPT-4 and Claude use decoder-only architectures with causal masking for autoregressive generation. The scaling laws show that performance improves predictably with more compute, data, and parameters, which is why we've seen the push toward larger models and longer context windows."
