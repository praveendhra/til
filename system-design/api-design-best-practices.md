# API Design Best Practices

## RESTful API Design

### URL Structure
```
✅ Good (nouns, hierarchical):
GET    /users                    # List users
POST   /users                    # Create user
GET    /users/123                # Get user 123
PUT    /users/123                # Update user 123 (full)
PATCH  /users/123                # Update user 123 (partial)
DELETE /users/123                # Delete user 123
GET    /users/123/orders         # List user 123's orders
GET    /users/123/orders/456     # Get specific order

❌ Bad (verbs, flat):
GET    /getUser?id=123
POST   /createUser
POST   /deleteUser/123
GET    /getUserOrders?userId=123
```

### HTTP Methods

| Method | Idempotent | Safe | Use Case |
|--------|-----------|------|----------|
| GET | ✅ | ✅ | Read resources |
| POST | ❌ | ❌ | Create resources |
| PUT | ✅ | ❌ | Full update (replace) |
| PATCH | ❌* | ❌ | Partial update |
| DELETE | ✅ | ❌ | Remove resource |
| HEAD | ✅ | ✅ | Check if resource exists |

*PATCH can be made idempotent with proper design.

**Idempotent**: Making the same request multiple times produces the same result.
- `DELETE /users/123` — First call deletes, subsequent calls return 404. Same end state = idempotent.
- `POST /users` — Each call creates a new user. Different end state = NOT idempotent.

### HTTP Status Codes

```
2xx: Success
  200 OK              GET/PUT/PATCH success
  201 Created          POST success (include Location header)
  204 No Content       DELETE success (no body needed)

3xx: Redirect
  301 Moved Permanently
  304 Not Modified     (cached response is still valid)

4xx: Client Error
  400 Bad Request      Invalid input, validation error
  401 Unauthorized     Not authenticated (no/invalid credentials)
  403 Forbidden        Authenticated but not authorized
  404 Not Found        Resource doesn't exist
  409 Conflict         Duplicate, version conflict
  422 Unprocessable    Validation error (semantic, not syntax)
  429 Too Many Requests  Rate limited

5xx: Server Error
  500 Internal Server Error   Unexpected error
  502 Bad Gateway      Upstream service failed
  503 Service Unavailable   Temporarily overloaded
  504 Gateway Timeout  Upstream service timed out
```

### Pagination

```
# Offset-based (simple but slow for large offsets)
GET /users?offset=100&limit=25

# Cursor-based (better performance, recommended)
GET /users?cursor=eyJpZCI6MTAwfQ&limit=25

Response:
{
  "data": [...],
  "pagination": {
    "next_cursor": "eyJpZCI6MTI1fQ",
    "has_more": true
  }
}
```

**Why cursor > offset?**
- Offset `1000000` requires the DB to skip 1M rows (slow)
- Cursor uses `WHERE id > last_seen_id LIMIT 25` (fast, uses index)

### Filtering, Sorting, Field Selection

```
# Filtering
GET /orders?status=active&min_total=100

# Sorting
GET /orders?sort=-created_at,+total    # Descending date, ascending total

# Field selection (sparse fieldsets)
GET /users/123?fields=name,email       # Only return these fields
```

### Versioning

```
# URL versioning (most common)
GET /v1/users
GET /v2/users

# Header versioning
GET /users
Accept: application/vnd.myapi.v2+json

# Query parameter
GET /users?version=2
```

**Recommendation**: URL versioning — simple, visible, cacheable.

### Error Responses

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input",
    "details": [
      {
        "field": "email",
        "message": "Must be a valid email address",
        "value": "not-an-email"
      },
      {
        "field": "age",
        "message": "Must be between 1 and 150",
        "value": -5
      }
    ],
    "request_id": "req-abc-123"  
  }
}
```

Always include a `request_id` for debugging.

### HATEOAS (Hypermedia)

```json
{
  "id": 123,
  "name": "John",
  "links": {
    "self": "/users/123",
    "orders": "/users/123/orders",
    "update": "/users/123",
    "delete": "/users/123"
  }
}
```

In practice, few APIs implement full HATEOAS. Include relevant links for discoverability.

## API Security

```
1. Always HTTPS (never HTTP)
2. Authentication: OAuth 2.0 / JWT for APIs, API keys for server-to-server
3. Rate limiting: Per API key/user, return 429 with Retry-After
4. Input validation: Validate all inputs server-side
5. CORS: Restrict allowed origins
6. Request size limits: Prevent abuse
7. Audit logging: Log all write operations
```

## API Documentation

Use **OpenAPI (Swagger)** specification:
```yaml
openapi: 3.0.0
info:
  title: My API
  version: 1.0.0
paths:
  /users:
    get:
      summary: List users
      parameters:
        - name: limit
          in: query
          schema:
            type: integer
            default: 25
      responses:
        '200':
          description: List of users
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/User'
```

## Interview Answer

> "I follow RESTful conventions: resources as nouns, HTTP methods for actions, proper status codes. For pagination, I use cursor-based pagination for performance — offset-based becomes slow with large datasets. Every response includes a request_id for debugging. I version APIs in the URL (/v1/, /v2/) for simplicity. For security, it's always HTTPS, OAuth 2.0 for authentication, rate limiting per API key, and input validation on every endpoint. The API contract is documented using OpenAPI spec, which auto-generates interactive documentation and client SDKs."
