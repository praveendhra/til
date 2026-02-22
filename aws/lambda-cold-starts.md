# Lambda Cold Starts - Causes and Mitigation

## What Is a Cold Start?
First invocation after idle period: AWS must provision container + download code + init runtime.

## Cold Start Duration
| Runtime | Cold Start |
|---------|-----------|
| Python | 200-500ms |
| Node.js | 200-500ms |
| Java | 1-3 seconds |
| .NET | 500ms-1.5s |
| Go | <100ms |
| Rust | <100ms |

## Mitigation Strategies

### 1. Provisioned Concurrency
Pre-warm N instances. Eliminates cold starts completely.
```
aws lambda put-provisioned-concurrency-config \
  --function-name my-func \
  --provisioned-concurrent-executions 10
```
**Cost**: ~$0.015/hr per instance. Use for latency-sensitive APIs.

### 2. Keep Functions Warm
CloudWatch Events rule to ping every 5 min. Hacky but free.

### 3. Optimize Package Size
- Use Lambda Layers for shared dependencies
- Tree-shake unused imports
- Use lighter frameworks (Flask > Django for Lambda)

### 4. Use SnapStart (Java only)
Pre-initializes JVM and creates a snapshot. Reduces cold start to ~200ms.

### 5. Choose Lighter Runtimes
Go/Rust have near-zero cold starts. Consider for latency-critical paths.
