# Load Balancer Algorithms Compared

## Layer 4 vs Layer 7

- **L4 (Transport)**: Routes based on IP/port. Faster, less flexible. (AWS NLB, Azure LB)
- **L7 (Application)**: Routes based on HTTP headers, URL, cookies. Smarter. (AWS ALB, Azure App Gateway)

## Algorithms

### Round Robin
Simplest. Rotates through servers sequentially. Works when servers are identical.

### Weighted Round Robin
Assigns weights based on server capacity. Server with weight 3 gets 3x traffic.

### Least Connections
Routes to server with fewest active connections. Great for long-lived connections (WebSockets).

### IP Hash
Hashes client IP to determine server. Ensures session stickiness without cookies.

### Random with Two Choices (Power of Two)
Pick 2 random servers, send to the one with fewer connections. Surprisingly effective.

## My Experience at Work
We use **F5/NGINX** with custom request header routing — similar to weighted round robin but route based on tenant ID in the header for multi-tenant isolation.
