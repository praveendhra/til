# GCP VPC Networking

## GCP VPCs are Global
Unlike AWS/Azure where VPCs/VNets are regional, **GCP VPCs span all regions**.

```
my-vpc (global)
├── subnet-us (us-central1, 10.0.1.0/24)
├── subnet-eu (europe-west1, 10.0.2.0/24)
└── subnet-asia (asia-east1, 10.0.3.0/24)
```

VMs in different regions but same VPC communicate via internal IPs automatically.

## Shared VPC
Centralize networking in a **host project**, share subnets with **service projects**.

```
Host Project (owns VPC)
├── Service Project A (uses shared subnets)
├── Service Project B (uses shared subnets)
└── Service Project C (uses shared subnets)
```

Network admins manage VPC centrally. App teams deploy in service projects.

## VPC Peering
- Connect VPCs across projects or organizations
- Non-transitive (like AWS)
- Max 25 peering connections per VPC

## Private Google Access
Access Google APIs (BigQuery, GCS, etc.) from VMs without public IPs.
Similar to AWS VPC Endpoints.

## Cloud NAT
Managed NAT for outbound internet from private VMs. No public IPs needed.

## Comparison
| Feature | GCP | AWS | Azure |
|---------|-----|-----|-------|
| VPC scope | Global | Regional | Regional |
| Centralized | Shared VPC | RAM sharing | Hub-spoke |
| NAT | Cloud NAT | NAT Gateway | NAT Gateway |
| Private API access | Private Google Access | VPC Endpoints | Private Endpoints |
