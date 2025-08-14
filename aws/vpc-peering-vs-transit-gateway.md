# VPC Peering vs Transit Gateway vs PrivateLink

## VPC Peering
- Direct connection between 2 VPCs
- No transitive routing (A↔B, B↔C does NOT mean A↔C)
- Cross-account and cross-region supported
- **Free** for same-region, data transfer charges for cross-region
- Good for: Simple 2-3 VPC setups

## Transit Gateway
- Hub-and-spoke model — connect many VPCs through one gateway
- Supports transitive routing
- Can connect VPNs and Direct Connect
- ~$0.05/hr + data processing charges
- Good for: Enterprise with 10+ VPCs, hub-and-spoke networking

## PrivateLink (VPC Endpoints)
- Access services privately without going through internet
- **Interface Endpoint**: ENI in your VPC → service (most AWS services)
- **Gateway Endpoint**: Route table entry → S3 or DynamoDB (free)
- Good for: Accessing AWS services or exposing your service to other VPCs

## Decision Matrix
| Need | Solution |
|------|----------|
| Connect 2 VPCs | VPC Peering |
| Connect 10+ VPCs | Transit Gateway |
| Access S3 privately | Gateway Endpoint |
| Expose service to customers | PrivateLink |
| Connect on-prem | Transit Gateway + VPN/DX |
