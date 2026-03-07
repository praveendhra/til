# MCP (Model Context Protocol) — Connecting AI to Tools

## What Is MCP?

An open protocol (by Anthropic) that standardizes how AI models connect to external tools and data sources. Think of it as **USB-C for AI** — one standard interface for all tools.

```
Before MCP:
  Each AI app builds custom integrations for each tool
  N apps × M tools = N×M integrations 😱

With MCP:
  Tools implement MCP server interface
  AI apps implement MCP client interface
  N apps + M tools = N+M integrations ✅
```

## Architecture

```
┌─────────────┐     MCP Protocol      ┌─────────────────┐
│  MCP Client │ ◄──────────────────► │  MCP Server      │
│  (AI App)   │    JSON-RPC over     │  (Tool Provider) │
│             │    stdio / SSE       │                  │
│  - Claude   │                      │  - File System   │
│  - VS Code  │                      │  - Database      │
│  - Custom   │                      │  - Kubernetes    │
└─────────────┘                      │  - GitHub        │
                                     └─────────────────┘
```

## Three Primitives

### 1. Tools (Model-Controlled)
Functions the AI model can call. Like function calling but standardized.

```python
@server.tool()
async def get_pod_status(namespace: str = "default") -> str:
    """Get status of all pods in a Kubernetes namespace."""
    result = subprocess.run(
        ["kubectl", "get", "pods", "-n", namespace, "-o", "json"],
        capture_output=True, text=True
    )
    return result.stdout
```

### 2. Resources (Application-Controlled)
Data the application can read (like files, DB records).

```python
@server.resource("file://{path}")
async def read_file(path: str) -> str:
    """Read a file from the workspace."""
    with open(path) as f:
        return f.read()
```

### 3. Prompts (User-Controlled)
Pre-built prompt templates for common workflows.

```python
@server.prompt()
async def troubleshoot_pod(pod_name: str, namespace: str) -> str:
    """Generate a troubleshooting prompt for a failing pod."""
    return f"""Troubleshoot the following Kubernetes pod:
    Pod: {pod_name}
    Namespace: {namespace}

    Steps:
    1. Check pod status and events
    2. Check container logs
    3. Check resource limits
    4. Check network policies
    Provide specific kubectl commands for each step."""
```

## Building an MCP Server (Python)

```python
from mcp.server import Server
from mcp.server.stdio import stdio_server

server = Server("devops-tools")

@server.tool()
async def kubectl_get(resource: str, namespace: str = "default") -> str:
    """Run kubectl get for a resource type."""
    result = subprocess.run(
        ["kubectl", "get", resource, "-n", namespace, "-o", "wide"],
        capture_output=True, text=True
    )
    if result.returncode != 0:
        return f"Error: {result.stderr}"
    return result.stdout

@server.tool()
async def docker_ps() -> str:
    """List running Docker containers."""
    result = subprocess.run(
        ["docker", "ps", "--format", "table {{.Names}}\t{{.Status}}\t{{.Ports}}"],
        capture_output=True, text=True
    )
    return result.stdout

async def main():
    async with stdio_server() as (read, write):
        await server.run(read, write)

if __name__ == "__main__":
    import asyncio
    asyncio.run(main())
```

## Connecting to VS Code / Claude

```json
// .vscode/mcp.json or claude_desktop_config.json
{
  "mcpServers": {
    "devops-tools": {
      "command": "python",
      "args": ["path/to/server.py"],
      "env": {
        "KUBECONFIG": "/path/to/kubeconfig"
      }
    }
  }
}
```

## MCP vs Function Calling vs Plugins

| Feature | MCP | Function Calling | ChatGPT Plugins |
|---------|-----|-----------------|-----------------|
| **Standard** | Open protocol | Vendor-specific | OpenAI only |
| **Transport** | stdio, SSE, HTTP | HTTP API | HTTP API |
| **Discovery** | Server advertises capabilities | Static tool definitions | Manifest file |
| **State** | Persistent connection | Stateless | Stateless |
| **Ecosystem** | Growing (open source) | Large (OpenAI) | Deprecated |

## Why MCP Matters

```
1. Standardization: Build once, works with any MCP client
2. Security: Clear permission model, server controls access
3. Composability: Chain multiple MCP servers together
4. Local-first: Servers can run locally (no cloud required)
5. Ecosystem: Growing library of pre-built servers
```

## Interview Answer

> "MCP is an open protocol that standardizes how AI applications connect to external tools. Instead of building custom integrations for each AI app and each tool, MCP defines a common interface — tools, resources, and prompts — so any MCP client (like VS Code Copilot or Claude) can use any MCP server (like a Kubernetes tool or database connector). I've built an MCP server for DevOps that exposes kubectl, Docker, and monitoring tools. The server runs locally via stdio transport and advertises its capabilities to the client. The key advantage over raw function calling is standardization and the persistent connection that maintains state across interactions."
