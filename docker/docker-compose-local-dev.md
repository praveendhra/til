# Docker Compose for Local Development

## Basic Structure

```yaml
# compose.yaml (v2 — no 'version' key needed)
services:
  api:
    build:
      context: .
      dockerfile: Dockerfile
      target: development        # multi-stage target
    ports:
      - "8000:8000"
    volumes:
      - .:/app                   # live reload
      - /app/node_modules        # anonymous volume (don't overwrite)
    environment:
      - DATABASE_URL=postgres://user:pass@db:5432/myapp
      - REDIS_URL=redis://cache:6379
    depends_on:
      db:
        condition: service_healthy
    develop:
      watch:
        - action: sync
          path: ./src
          target: /app/src
        - action: rebuild
          path: ./package.json

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_USER: user
      POSTGRES_PASSWORD: pass
      POSTGRES_DB: myapp
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U user"]
      interval: 5s
      timeout: 3s
      retries: 5

  cache:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  pgdata:
```

## Key Commands

```bash
docker compose up -d              # start detached
docker compose up --build         # rebuild images first
docker compose down               # stop and remove containers
docker compose down -v            # also remove volumes (reset data)
docker compose logs api -f        # follow service logs
docker compose exec api sh        # shell into running container
docker compose ps                 # list running services
docker compose watch              # file sync mode (Compose 2.22+)
```

## Profiles — Optional Services

```yaml
services:
  api:
    # always starts
  
  debug-ui:
    image: pgadmin4
    profiles: ["debug"]          # only with --profile debug

  mailpit:
    image: axllent/mailpit
    profiles: ["debug"]
```

```bash
docker compose --profile debug up
```

## Multiple Compose Files (Override Pattern)

```bash
# compose.yaml          — base config
# compose.override.yaml — auto-loaded, dev settings
# compose.prod.yaml     — production overrides

docker compose -f compose.yaml -f compose.prod.yaml up
```

## Networking

- Services on same compose network can reach each other by service name
- `db:5432` works from inside `api` container
- Exposed ports (`ports:`) are for host access only

## Health Checks + Dependency Ordering

```yaml
depends_on:
  db:
    condition: service_healthy    # wait for healthcheck to pass
  cache:
    condition: service_started    # just wait for container start
```

## Tips

- Use `develop.watch` instead of bind mounts for better performance on macOS
- Name your volumes to persist data across `down`/`up`
- Use `.env` file for shared variables (auto-loaded)
- Use `extends` to share common config between services
