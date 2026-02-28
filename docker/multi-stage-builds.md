# Docker Multi-Stage Builds

## Problem
Build dependencies bloat production images. A Java build needs Maven + JDK (800MB+), but runtime only needs JRE (200MB).

## Solution: Multi-Stage Builds
```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
COPY --from=builder /app/target/app.jar /app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app.jar"]
```

## Results
| Stage | Image Size |
|-------|-----------|
| Builder | ~850 MB |
| Runtime | ~180 MB |
| **Reduction** | **~79%** |

## Python Example
```dockerfile
FROM python:3.12-slim AS builder
WORKDIR /app
COPY requirements.txt .
RUN pip install --user -r requirements.txt

FROM python:3.12-slim
COPY --from=builder /root/.local /root/.local
COPY . /app
ENV PATH=/root/.local/bin:$PATH
CMD ["python", "/app/main.py"]
```

## Best Practices
- Copy dependency files first (cache layer)
- Use `.dockerignore` to exclude unnecessary files
- Use `alpine` or `slim` variants for runtime
- Don't install dev tools in runtime stage
