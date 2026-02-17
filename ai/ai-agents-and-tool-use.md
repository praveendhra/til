# AI Agents — Autonomous LLM Systems with Tool Use

## What Is an AI Agent?

An LLM that can **reason**, **plan**, **use tools**, and **take actions** autonomously.

```
Traditional LLM:
  Input → Model → Text Output (that's it)

AI Agent:
  Input → Model → Thinks → Uses Tool → Observes Result
       → Thinks Again → Uses Another Tool → ...
       → Final Answer (grounded in real data/actions)
```

## The Agent Loop (ReAct Pattern)

```python
while not done:
    # 1. REASON: What should I do next?
    thought = llm.think(observation)

    # 2. ACT: Choose and execute a tool
    action = llm.choose_tool(thought)
    result = execute_tool(action)

    # 3. OBSERVE: What happened?
    observation = result

    # 4. Check: Am I done?
    if llm.is_task_complete(observation):
        done = True
        return llm.format_answer(observation)
```

### Example: "What's the weather in the city where Apple HQ is?"

```
Thought: I need to find where Apple HQ is located.
Action: search("Apple headquarters location")
Observation: Apple Park, Cupertino, California

Thought: Now I need the weather in Cupertino.
Action: get_weather("Cupertino, CA")
Observation: 72°F, sunny, humidity 45%

Thought: I have all the information needed.
Answer: The weather in Cupertino, CA (Apple HQ) is 72°F and sunny.
```

## Function Calling (Tool Use API)

Modern LLMs support structured tool definitions:

```python
tools = [
    {
        "type": "function",
        "function": {
            "name": "get_weather",
            "description": "Get current weather for a location",
            "parameters": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "City and state, e.g. 'Austin, TX'"
                    },
                    "unit": {
                        "type": "string",
                        "enum": ["fahrenheit", "celsius"]
                    }
                },
                "required": ["location"]
            }
        }
    }
]

response = client.chat.completions.create(
    model="gpt-4",
    messages=[{"role": "user", "content": "Weather in Austin?"}],
    tools=tools,
    tool_choice="auto"
)
# Model returns: {"name": "get_weather", "arguments": {"location": "Austin, TX"}}
```

## Agent Architectures

### 1. Single Agent (Simple)
```
User → [Agent with tools] → Result
Good for: Simple tasks, well-defined tool sets
```

### 2. Router Agent
```
User → [Router] → Agent A (code tasks)
                → Agent B (research tasks)
                → Agent C (data analysis)
Good for: Diverse task types
```

### 3. Multi-Agent (Collaborative)
```
User → [Orchestrator]
         → [Researcher Agent] → findings
         → [Analyst Agent] → analysis
         → [Writer Agent] → final report
Good for: Complex workflows requiring different expertise
```

### 4. Hierarchical (Manager-Worker)
```
[Manager Agent]
  ├── [Worker 1: Fetch data]
  ├── [Worker 2: Process data]
  └── [Worker 3: Generate report]
Good for: Breaking complex tasks into subtasks
```

## Common Agent Tools

```
Information retrieval:
  - Web search (Tavily, Brave, Google)
  - Database queries (SQL, vector search)
  - API calls (REST, GraphQL)
  - File reading/writing

Code execution:
  - Python REPL
  - Shell commands
  - Code interpreters

Communication:
  - Email sending
  - Slack messages
  - GitHub issue creation

DevOps specific:
  - kubectl commands
  - Terraform operations
  - Docker management
  - Log analysis
```

## Agent Memory

```
Short-term (conversation): Current dialogue context
  → Chat history, working variables

Long-term (persistent): Past interactions, learned preferences
  → Vector DB, key-value store, graph DB

Working memory: Intermediate results during task execution
  → Scratchpad, task state
```

## Challenges & Solutions

| Challenge | Problem | Solution |
|-----------|---------|----------|
| **Infinite loops** | Agent keeps trying the same thing | Max iterations, timeout |
| **Tool errors** | API fails, wrong parameters | Error handling, retry with correction |
| **Hallucinated tools** | Agent invents non-existent tools | Strict tool schema validation |
| **Cost explosion** | Too many LLM calls | Budget limits, caching |
| **Security** | Agent executes dangerous commands | Sandboxing, approval workflow |
| **Evaluation** | Hard to test non-deterministic behavior | Trajectory evaluation, benchmarks |

## Frameworks

| Framework | Language | Best For |
|-----------|----------|----------|
| **LangChain/LangGraph** | Python/JS | General purpose, graphs |
| **CrewAI** | Python | Multi-agent collaboration |
| **AutoGen** | Python | Multi-agent conversations |
| **Semantic Kernel** | C#/Python | Enterprise, Microsoft stack |
| **Haystack** | Python | Search/RAG focused |
| **Vercel AI SDK** | TypeScript | Web applications |

## Interview Answer

> "AI agents are LLMs that can reason, plan, and use tools autonomously in a loop: think → act → observe → repeat. I implement the ReAct pattern where the model decides which tool to call, observes the result, and continues until the task is complete. For complex tasks, I use multi-agent architectures where specialized agents collaborate — one for research, one for analysis, one for writing. Key challenges are preventing infinite loops (max iterations), handling tool errors gracefully, and controlling costs (budget limits per task). For production, I use LangGraph for stateful agent workflows with checkpointing and human-in-the-loop approval for critical actions."
