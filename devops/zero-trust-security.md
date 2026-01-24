# Zero Trust Security

## Core Principle
**"Never trust, always verify."** No implicit trust based on network location.

## Traditional vs Zero Trust
| Traditional (Perimeter) | Zero Trust |
|------------------------|------------|
| Trust inside firewall | Trust nothing |
| VPN = trusted | Every request verified |
| Flat internal network | Micro-segmented |
| IP-based access | Identity-based access |

## Pillars
1. **Identity verification**: MFA, SSO, conditional access
2. **Device trust**: Is the device compliant, patched, managed?
3. **Least privilege access**: Just enough permissions, just in time
4. **Micro-segmentation**: Limit lateral movement
5. **Continuous monitoring**: Log and analyze everything

## Cloud Implementation

### AWS
- IAM + STS for short-lived credentials
- Security Groups (micro-segmentation)
- VPC Flow Logs + GuardDuty
- AWS SSO + MFA

### Azure
- Azure AD Conditional Access
- Entra ID + PIM (just-in-time access)
- NSG + Azure Firewall
- Microsoft Sentinel (SIEM)

### GCP
- BeyondCorp Enterprise (Google's zero trust)
- IAP (Identity-Aware Proxy)
- VPC Service Controls
- Binary Authorization (verified container images)

## Key Takeaway
Zero trust isn't a product — it's an architecture. Start with identity, then add layers.
