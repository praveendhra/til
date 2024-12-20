# Circuit Breaker Pattern

## Problem
Cascading failures when a downstream service is unavailable can bring down the entire system.

## Solution
The circuit breaker monitors failures and "trips" when a threshold is exceeded, preventing further calls.

## States
1. **Closed** – Requests flow normally; failures are counted
2. **Open** – All requests fail fast without calling the service
3. **Half-Open** – A limited number of test requests are allowed through

## Implementation with Resilience4j
```java
CircuitBreakerConfig config = CircuitBreakerConfig.custom()
    .failureRateThreshold(50)            // 50% failure rate
    .waitDurationInOpenState(Duration.ofSeconds(30))
    .slidingWindowSize(10)
    .minimumNumberOfCalls(5)
    .build();

CircuitBreaker cb = CircuitBreaker.of("paymentService", config);
Supplier<String> decorated = CircuitBreaker.decorateSupplier(cb, () -> paymentService.charge());
```

## Key Metrics
- Failure rate threshold (typically 50%)
- Wait duration in open state (30-60s)
- Sliding window size (10-100 requests)
- Minimum number of calls before evaluation

## When to Use
- Calls to external APIs or microservices
- Database connections under heavy load
- Any remote call that could fail or timeout
