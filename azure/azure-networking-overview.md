# Azure Networking - VNet, NSG, and Private Endpoints

## Virtual Network (VNet)
Azure's isolated network. Similar to AWS VPC.

### Key Concepts
- **Subnets**: Segment VNet (e.g., web-tier, app-tier, db-tier)
- **Address space**: CIDR blocks (e.g., 10.0.0.0/16)
- **VNet Peering**: Connect VNets (supports cross-region, transitive via hub-spoke)
- **Service Endpoints**: Direct path from subnet to Azure services

## Network Security Groups (NSG)
Stateful firewall at subnet or NIC level.
```
Priority | Direction | Action | Source      | Dest   | Port
100      | Inbound   | Allow  | 10.0.1.0/24| Any    | 443
200      | Inbound   | Allow  | AzureLB    | Any    | 80
65500    | Inbound   | Deny   | Any        | Any    | Any
```

## Private Endpoints
Access Azure services (Storage, SQL, Cosmos DB) over private IP in your VNet.
No traffic over the internet.

```
Your VNet (10.0.0.0/16)
  └── Private Endpoint (10.0.1.5) → Azure SQL Database
```

**vs Service Endpoints**: Private Endpoints give you a private IP in your VNet. Service Endpoints just route traffic over Azure backbone but service still has public IP.

## Hub-Spoke Topology
```
        [Hub VNet]
       /    |     \
[Spoke 1] [Spoke 2] [Spoke 3]
```
Hub has: Firewall, VPN Gateway, Bastion
Spokes have: Workloads
Use **Azure Virtual WAN** for managed hub-spoke at scale.
