# Open Source LLMs & Self-Hosting — Running Models Locally

## Why Self-Host?

```
✅ Data privacy: Sensitive data never leaves your infrastructure
✅ Cost: No per-token charges (fixed infrastructure cost)
✅ Latency: No network round-trip to API provider
✅ Customization: Fine-tune for your specific use case
✅ No rate limits: Scale based on your hardware
✅ Compliance: Meet data residency requirements

❌ High upfront cost: GPU infrastructure
❌ Operational burden: Maintain, update, scale
❌ Smaller models: Open source lags behind GPT-4/Claude
```

## Top Open Source Models (2026)

| Model | Parameters | Context | License | Best For |
|-------|-----------|---------|---------|----------|
| LLaMA 3.1 | 8B / 70B / 405B | 128K | Meta license | General purpose |
| Mistral | 7B / 8x7B (MoE) | 32K | Apache 2.0 | Efficiency |
| Qwen 2.5 | 7B / 72B | 128K | Apache 2.0 | Multilingual |
| DeepSeek V3 | 685B (MoE) | 128K | MIT | Coding, reasoning |
| Phi-3 | 3.8B / 14B | 128K | MIT | Small & capable |
| Gemma 2 | 2B / 9B / 27B | 8K | Gemma license | Lightweight |

## Inference Servers

### vLLM (Recommended)
```bash
pip install vllm

# Serve a model with OpenAI-compatible API
python -m vllm.entrypoints.openai.api_server \
    --model meta-llama/Meta-Llama-3.1-8B-Instruct \
    --tensor-parallel-size 2 \
    --max-model-len 8192

# Key features:
# - PagedAttention (efficient memory management)
# - Continuous batching (high throughput)
# - OpenAI-compatible API (drop-in replacement)
# - Tensor parallelism (split across GPUs)
```

### Ollama (Easy Local Setup)
```bash
# Install and run
curl -fsSL https://ollama.ai/install.sh | sh
ollama run llama3.1

# API
curl http://localhost:11434/api/generate \
    -d '{"model": "llama3.1", "prompt": "Explain Docker"}'

# Great for development and testing
```

### Text Generation Inference (TGI) by HuggingFace
```bash
docker run --gpus all -p 8080:80 \
    ghcr.io/huggingface/text-generation-inference:latest \
    --model-id meta-llama/Meta-Llama-3.1-8B-Instruct

# Production-grade, used by HuggingFace Inference Endpoints
```

## Hardware Requirements

```
Model size → VRAM needed:
  7B parameters × FP16 (2 bytes) = 14 GB VRAM
  13B → 26 GB | 70B → 140 GB | 405B → 810 GB

With quantization (4-bit):
  7B → ~4 GB | 13B → ~7 GB | 70B → ~35 GB

GPU options:
  NVIDIA A100 (80GB): 1-2 for 70B models
  NVIDIA H100 (80GB): Fastest, expensive
  NVIDIA A10G (24GB): Good for 7B-13B
  NVIDIA T4 (16GB): Budget option for 7B
  Apple M2/M3 Ultra: 128-192 GB unified memory (!)
```

## Quantization

Reduce model precision to use less memory and run faster:

```
FP32 (full):    4 bytes per param → baseline quality, huge memory
FP16 (half):    2 bytes per param → ~same quality, half memory
INT8:           1 byte per param  → slight quality loss, 4x less memory
INT4 (GPTQ/AWQ): 0.5 bytes       → noticeable for complex tasks, 8x less

GGUF format (llama.cpp):
  Q4_K_M: Good balance of quality and speed
  Q5_K_M: Better quality, slightly slower
  Q8_0: Near-original quality
```

## Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: llm-server
spec:
  replicas: 2
  selector:
    matchLabels:
      app: llm-server
  template:
    spec:
      containers:
      - name: vllm
        image: vllm/vllm-openai:latest
        args:
          - --model=meta-llama/Meta-Llama-3.1-8B-Instruct
          - --max-model-len=8192
        resources:
          limits:
            nvidia.com/gpu: 1
        ports:
        - containerPort: 8000
      tolerations:
      - key: nvidia.com/gpu
        operator: Exists
        effect: NoSchedule
```

## Interview Answer

> "I self-host LLMs when data privacy or compliance requires it — sensitive data never leaves our infrastructure. I use vLLM as the inference server for its PagedAttention memory management and continuous batching, which maximize GPU utilization. For a 70B parameter model, I need two A100 80GB GPUs with tensor parallelism. For cost efficiency, I quantize to INT4 using GPTQ, which reduces memory 8x with acceptable quality loss for most tasks. The vLLM server exposes an OpenAI-compatible API, so switching between self-hosted and OpenAI is a config change. In Kubernetes, I deploy with GPU node pools, tolerations, and HPA based on queue depth."
