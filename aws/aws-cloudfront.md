# AWS CloudFront CDN

## Key Concepts
- **Distribution**: A CloudFront configuration (edge locations → origin)
- **Origin**: Where content comes from (S3, ALB, custom HTTP)
- **Behavior**: Rules for how CloudFront handles requests
- **Edge Location**: Point of presence closest to user

## Cache Behaviors
```
/api/*     → ALB origin (no caching, TTL=0)
/static/*  → S3 origin (cache 1 year, immutable)
/*         → S3 origin (cache 1 day)
```

## Cache Invalidation
```bash
aws cloudfront create-invalidation \
  --distribution-id E1234567 \
  --paths "/index.html" "/css/*"
```

## Origin Access Control (OAC)
Restrict S3 access to CloudFront only:
```json
{
  "Statement": [{
    "Effect": "Allow",
    "Principal": {"Service": "cloudfront.amazonaws.com"},
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::my-bucket/*",
    "Condition": {
      "StringEquals": {
        "AWS:SourceArn": "arn:aws:cloudfront::123456:distribution/E1234567"
      }
    }
  }]
}
```

## CloudFront Functions vs Lambda@Edge
| Feature | CF Functions | Lambda@Edge |
|---------|-------------|-------------|
| Runtime | JavaScript | Node.js, Python |
| Duration | < 1ms | Up to 30s |
| Memory | 2MB | Up to 10GB |
| Network | No | Yes |
| Cost | 1/6th of Lambda@Edge | Higher |
| Use case | Header manipulation | Auth, A/B testing |
