# Docker Best Practices — Multi-Stage Builds, Security, Performance

## Multi-Stage Builds

Separate build dependencies from runtime → smaller, more secure images.

```dockerfile
# ── Stage 1: Build ──
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --production=false   # Install ALL deps (including devDependencies)
COPY . .
RUN npm run build               # Compile TypeScript, bundle, etc.

# ── Stage 2: Runtime ──
FROM node:20-alpine
RUN addgroup -g 1001 appgroup && adduser -u 1001 -G appgroup -s /bin/sh -D appuser
WORKDIR /app
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/node_modules ./node_modules
COPY package*.json ./
USER appuser
EXPOSE 3000
CMD ["node", "dist/server.js"]
```

### Size Comparison

```
Without multi-stage:
  node:20 + source + devDependencies + build tools = ~1.2 GB

With multi-stage:
  node:20-alpine + dist + production deps only = ~150 MB

Distroless or scratch:
  Even smaller for Go/Rust: ~10-50 MB
```

## Layer Caching (Critical for Build Speed)

Docker caches each layer. If a layer's input hasn't changed, it's reused.

```dockerfile
# ❌ Bad: Any source code change invalidates npm install cache
COPY . .
RUN npm install

# ✅ Good: Only invalidate npm install when package.json changes
COPY package*.json ./
RUN npm ci
COPY . .                # Source code changes don't re-run npm ci
```

### Layer Order Rule
**Put things that change LEAST at the TOP, things that change MOST at the BOTTOM.**

```dockerfile
FROM python:3.12-slim
WORKDIR /app

# Layer 1: System deps (rarely change)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl && rm -rf /var/lib/apt/lists/*

# Layer 2: Python deps (change occasionally)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Layer 3: Source code (changes frequently)
COPY . .

CMD ["python", "app.py"]
```

## Security Best Practices

### 1. Don't Run as Root
```dockerfile
# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser
```

### 2. Use Minimal Base Images
```
Image                    Size        CVEs
ubuntu:22.04             ~77 MB      Many
python:3.12              ~1 GB       Many
python:3.12-slim         ~130 MB     Fewer
python:3.12-alpine       ~50 MB      Fewest
gcr.io/distroless/python3  ~50 MB    Minimal
scratch                  0 MB        Zero (for Go/Rust)
```

### 3. Pin Image Versions
```dockerfile
# ❌ Bad: "latest" can change unexpectedly
FROM python:latest

# ✅ Good: Pin to specific version
FROM python:3.12.3-slim-bookworm

# ✅✅ Best: Pin to digest (immutable)
FROM python@sha256:abc123...
```

### 4. Scan for Vulnerabilities
```bash
# Trivy
trivy image myapp:latest

# Docker Scout
docker scout cves myapp:latest

# Snyk
snyk container test myapp:latest
```

### 5. Use .dockerignore
```
# .dockerignore
.git
node_modules
*.md
.env
.env.*
tests/
coverage/
.github/
```

### 6. No Secrets in Images
```dockerfile
# ❌ NEVER: Secrets baked into image (visible in layer history)
ENV DATABASE_PASSWORD=secret123
COPY .env .

# ✅ Pass at runtime
# docker run -e DATABASE_URL=... myapp
# Or use Docker secrets / Kubernetes Secrets
```

## Go Multi-Stage (Scratch Image)

```dockerfile
FROM golang:1.22-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o /server ./cmd/server

FROM scratch
COPY --from=builder /server /server
COPY --from=builder /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/
EXPOSE 8080
ENTRYPOINT ["/server"]
# Final image: ~10-15 MB!
```

## Health Checks

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:3000/health || exit 1
```

## docker-compose for Development

```yaml
services:
  app:
    build:
      context: .
      target: builder    # Use builder stage for dev (hot reload)
    volumes:
      - .:/app           # Mount source for live editing
      - /app/node_modules # Don't override node_modules
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=development
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: myapp
      POSTGRES_USER: myapp
      POSTGRES_PASSWORD: localdev   # OK for local dev only!
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U myapp"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  pgdata:
```

## Interview Answer

> "I use multi-stage Docker builds to separate build dependencies from runtime — a Node.js app goes from 1.2GB to ~150MB. Layer ordering is critical for cache efficiency: system deps first, then package dependencies, then source code last. For security, I never run as root, pin base image versions, scan with Trivy in CI, and never bake secrets into images. For Go services, I use scratch base images for ~10MB final images with zero CVEs. In Kubernetes, I always set resource limits, health checks, and use read-only root filesystems."
