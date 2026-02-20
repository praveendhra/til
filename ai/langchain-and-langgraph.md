# LangChain & LangGraph — Building LLM Applications

## LangChain — The Building Blocks

LangChain provides composable abstractions for LLM applications.

### Core Components

```python
# 1. LLM / Chat Model
from langchain_openai import ChatOpenAI
llm = ChatOpenAI(model="gpt-4", temperature=0)

# 2. Prompt Templates
from langchain_core.prompts import ChatPromptTemplate
prompt = ChatPromptTemplate.from_messages([
    ("system", "You are a helpful DevOps assistant."),
    ("human", "{question}")
])

# 3. Output Parsers
from langchain_core.output_parsers import JsonOutputParser
parser = JsonOutputParser()

# 4. Chain (LCEL — LangChain Expression Language)
chain = prompt | llm | parser
result = chain.invoke({"question": "Explain K8s pods"})
```

### LCEL (LangChain Expression Language)

Pipe syntax for composing chains:

```python
# Simple chain
chain = prompt | llm | output_parser

# With retrieval (RAG)
chain = (
    {"context": retriever, "question": RunnablePassthrough()}
    | prompt
    | llm
    | StrOutputParser()
)

# Parallel execution
chain = RunnableParallel(
    summary=summary_chain,
    translation=translation_chain,
    sentiment=sentiment_chain
)
# All three run in parallel!
```

### RAG with LangChain

```python
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_openai import OpenAIEmbeddings
from langchain_community.vectorstores import Chroma

# 1. Load documents
loader = PyPDFLoader("docs/handbook.pdf")
docs = loader.load()

# 2. Split into chunks
splitter = RecursiveCharacterTextSplitter(
    chunk_size=500, chunk_overlap=50
)
chunks = splitter.split_documents(docs)

# 3. Create vector store
embeddings = OpenAIEmbeddings()
vectorstore = Chroma.from_documents(chunks, embeddings)

# 4. Create retriever
retriever = vectorstore.as_retriever(
    search_type="mmr",  # Maximal Marginal Relevance
    search_kwargs={"k": 5}
)

# 5. RAG chain
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnablePassthrough

template = """Answer based only on this context:
{context}

Question: {question}"""

prompt = ChatPromptTemplate.from_template(template)

rag_chain = (
    {"context": retriever, "question": RunnablePassthrough()}
    | prompt
    | llm
    | StrOutputParser()
)

answer = rag_chain.invoke("What is our vacation policy?")
```

## LangGraph — Stateful Agent Workflows

LangGraph extends LangChain for **cyclic, stateful** workflows (agents).

### Why LangGraph over LangChain Agents?

```
LangChain AgentExecutor:
  ✅ Simple tool-calling loop
  ❌ Linear flow only
  ❌ No state management
  ❌ No human-in-the-loop
  ❌ Hard to customize control flow

LangGraph:
  ✅ Graph-based control flow (cycles, branches)
  ✅ Persistent state across steps
  ✅ Human-in-the-loop built in
  ✅ Checkpointing and resumability
  ✅ Streaming support
```

### Basic Agent with LangGraph

```python
from langgraph.graph import StateGraph, START, END
from langgraph.prebuilt import ToolNode
from typing import TypedDict, Annotated
from langgraph.graph.message import add_messages

# 1. Define state
class AgentState(TypedDict):
    messages: Annotated[list, add_messages]

# 2. Define nodes
def call_model(state: AgentState):
    response = llm_with_tools.invoke(state["messages"])
    return {"messages": [response]}

def should_continue(state: AgentState):
    last_message = state["messages"][-1]
    if last_message.tool_calls:
        return "tools"
    return END

# 3. Build graph
graph = StateGraph(AgentState)
graph.add_node("agent", call_model)
graph.add_node("tools", ToolNode(tools))

graph.add_edge(START, "agent")
graph.add_conditional_edges("agent", should_continue)
graph.add_edge("tools", "agent")  # After tools, go back to agent

# 4. Compile and run
app = graph.compile()
result = app.invoke({
    "messages": [("human", "What's the weather in Austin?")]
})
```

### Human-in-the-Loop

```python
from langgraph.checkpoint.memory import MemorySaver

# Add checkpointing
memory = MemorySaver()
app = graph.compile(
    checkpointer=memory,
    interrupt_before=["tools"]  # Pause before executing tools
)

# Run until interrupt
config = {"configurable": {"thread_id": "1"}}
result = app.invoke({"messages": [("human", "Delete all pods")]}, config)

# Human reviews the tool call...
# If approved, resume:
app.invoke(None, config)  # Continue from checkpoint
```

## LangSmith — Observability

```python
# Set environment variables
os.environ["LANGCHAIN_TRACING_V2"] = "true"
os.environ["LANGCHAIN_API_KEY"] = "..."

# All chain/agent calls are automatically traced:
# - Input/output at each step
# - Latency per component
# - Token usage and cost
# - Error tracking
```

## Interview Answer

> "I use LangChain for composing LLM pipelines with LCEL — the pipe syntax makes it easy to chain prompts, models, and output parsers. For RAG, I combine document loaders, text splitters, embeddings, and vector stores into a retrieval chain. When I need complex agent workflows with cycles, state management, and human-in-the-loop approval, I use LangGraph. It represents the workflow as a graph where nodes are LLM calls or tool executions, and edges define control flow. The key advantage over plain LangChain agents is checkpointing — I can pause execution before critical actions, get human approval, and resume from exactly where it stopped. I use LangSmith for tracing and debugging in production."
