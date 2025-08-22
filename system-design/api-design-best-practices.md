# REST API Design Best Practices

## URL Structure
```
GET    /api/v1/users          # List users
GET    /api/v1/users/123      # Get user
POST   /api/v1/users          # Create user
PUT    /api/v1/users/123      # Update user (full)
PATCH  /api/v1/users/123      # Update user (partial)
DELETE /api/v1/users/123      # Delete user
```

## Pagination
```json
GET /api/v1/users?page=2&limit=20

{
  "data": [...],
  "meta": {
    "page": 2,
    "limit": 20,
    "total": 150,
    "next": "/api/v1/users?page=3&limit=20"
  }
}
```

Cursor-based for large datasets:
```
GET /api/v1/users?cursor=eyJpZCI6MTIzfQ&limit=20
```

## Versioning
- URL path: `/api/v1/users` (most common)
- Header: `Accept: application/vnd.api+json;version=1`

## Error Responses
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Email is required",
    "details": [
      {"field": "email", "message": "must not be blank"}
    ]
  }
}
```

## Rate Limiting Headers
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 42
X-RateLimit-Reset: 1625097600
```

## Idempotency
Use `Idempotency-Key` header for POST requests to safely retry.
Stripe and AWS both use this pattern.
