# CDN Architecture and Cache Invalidation

## How CDNs Work
1. User requests `static.example.com/image.png`
2. DNS resolves to nearest **edge location** (PoP)
3. Edge checks its cache → HIT: return immediately
4. MISS: fetch from **origin server**, cache it, return to user

## Cache Invalidation Strategies

### 1. TTL-based
Set `Cache-Control: max-age=86400` (24h). Simple but can serve stale content.

### 2. Versioned URLs
`/js/app.v2.3.1.js` — new deploy = new URL. Cache never stale. **Best practice for static assets.**

### 3. Purge/Invalidate API
Force CDN to drop cached content. Useful for emergency fixes.
- CloudFront: `create-invalidation`
- Azure CDN: `Purge` endpoint
- Cloud CDN: `gcloud compute url-maps invalidate-cdn-cache`

## CDN Services Compared
| Feature | CloudFront | Azure CDN | Cloud CDN |
|---------|-----------|-----------|-----------|
| Edge locations | 450+ | 130+ | 150+ |
| Origin | S3, ALB, custom | Blob, App Service | GCS, GCE |
| WAF integration | AWS WAF | Azure WAF | Cloud Armor |
| Real-time logs | Yes (Kinesis) | Yes (Diagnostics) | Yes (Logging) |
