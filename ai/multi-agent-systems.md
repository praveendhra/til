# Multi-Agent Systems — Collaborative AI Architectures

## Why Multiple Agents?

Single agents struggle with complex, multi-step tasks. Specialized agents collaborating perform better.

```
Single Agent:
  "Research competitor pricing, analyze trends, write a report,
   create charts, and email stakeholders"
  → Tries to do everything → mediocre at all of it

Multi-Agent:
  Researcher → Analyst → Writer → Reviewer
  Each agent is specialized → better overall quality
```

## Orchestration Patterns

### 1. Sequential (Pipeline)
```
Agent A → Agent B → Agent C → Final Output
(Research)  (Analyze)  (Write)

Each agent's output feeds the next agent's input.
Simple, predictable, easy to debug.
```

### 2. Hierarchical (Manager-Worker)
```
        [Manager Agent]
       /      |        \
  [Worker 1] [Worker 2] [Worker 3]
  (Research)  (Code)     (Review)

Manager breaks task into subtasks, delegates, synthesizes results.
Good for complex tasks with independent subtasks.
```

### 3. Debate/Adversarial
```
[Proposer Agent] ←→ [Critic Agent]
  Proposes solution    Reviews and critiques
  Revises based        Points out flaws
  on feedback          Suggests improvements

Iterates until consensus. Good for code review, fact-checking.
```

### 4. Voting/Ensemble
```
  [Agent 1] → Answer A
  [Agent 2] → Answer B  → [Aggregator] → Best Answer
  [Agent 3] → Answer C

Multiple agents answer independently, aggregator picks the best.
Good for reducing errors, increasing reliability.
```

## CrewAI Example

```python
from crewai import Agent, Task, Crew, Process

# Define specialized agents
researcher = Agent(
    role="Senior Research Analyst",
    goal="Find comprehensive information about {topic}",
    backstory="Expert at gathering and synthesizing information "
              "from multiple sources.",
    tools=[search_tool, web_scraper],
    llm=ChatOpenAI(model="gpt-4")
)

analyst = Agent(
    role="Technical Analyst",
    goal="Analyze research findings and identify key insights",
    backstory="Data-driven analyst who excels at finding patterns "
              "and drawing actionable conclusions.",
    llm=ChatOpenAI(model="gpt-4")
)

writer = Agent(
    role="Technical Writer",
    goal="Create a clear, well-structured report",
    backstory="Expert at translating complex technical topics "
              "into accessible, engaging content.",
    llm=ChatOpenAI(model="gpt-4")
)

# Define tasks
research_task = Task(
    description="Research {topic}: key features, market position, "
                "technical architecture, and community adoption.",
    expected_output="Detailed research notes with sources",
    agent=researcher
)

analysis_task = Task(
    description="Analyze the research findings. Identify strengths, "
                "weaknesses, and recommendations.",
    expected_output="Structured analysis with key insights",
    agent=analyst
)

report_task = Task(
    description="Write a comprehensive report combining research "
                "and analysis. Include executive summary.",
    expected_output="Professional report in markdown format",
    agent=writer
)

# Create crew and run
crew = Crew(
    agents=[researcher, analyst, writer],
    tasks=[research_task, analysis_task, report_task],
    process=Process.sequential,
    verbose=True
)

result = crew.kickoff(inputs={"topic": "LangGraph vs CrewAI"})
```

## Agent Communication Patterns

```python
# Shared memory (blackboard)
class SharedState:
    research_notes: str = ""
    analysis: str = ""
    draft_report: str = ""
    feedback: list = []

# Message passing
agent_a.send(agent_b, Message(
    type="research_complete",
    content=research_notes
))

# Tool delegation
# Agent A calls Agent B as a tool
@tool
def ask_researcher(question: str) -> str:
    return researcher_agent.invoke(question)
```

## Challenges

| Challenge | Solution |
|-----------|----------|
| Agents contradicting each other | Clear role definitions, arbitration agent |
| Infinite delegation loops | Max delegation depth, timeout |
| Context loss between agents | Shared state, structured handoff |
| Cost multiplication | Budget per agent, cheaper models for simple tasks |
| Debugging multi-agent flows | Structured logging, LangSmith traces |

## Interview Answer

> "Multi-agent systems use specialized agents that collaborate on complex tasks. I typically use a sequential pipeline — researcher, analyst, writer — where each agent's output feeds the next. For more complex workflows, I use hierarchical orchestration where a manager agent breaks tasks into subtasks and delegates to workers. I implement this with CrewAI for its simplicity or LangGraph for more control over state and flow. Key challenges are preventing infinite delegation loops (max depth limits), maintaining context across agents (shared state), and controlling costs (use cheaper models for simpler subtasks). The main benefit over single agents is specialization — each agent has focused instructions and tools for its specific role."
