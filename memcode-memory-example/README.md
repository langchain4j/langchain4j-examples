# MemCode-backed long-term user memory example

This example shows how to combine LangChain4j's ordinary `ChatMemory` with
[MemCode](https://memcode.in) as a long-term, cross-conversation user memory:

1. `ChatMemory` (`MessageWindowChatMemory`) stays responsible only for the current dialogue.
2. Relevant MemCode entries are retrieved **before** the assistant is invoked and injected
   as clearly delimited, untrusted reference data.
3. A fact is written to long-term memory only through an **explicit application action**
   after user confirmation. The assistant's output is never saved automatically. Revising a
   fact uses the same path: the approved action ingests the updated text and MemCode
   reconciles it server-side.
4. Memory ownership is scoped **server-side** by the MemCode credential; the application
   never sends a user id, so it cannot address another user's memory.

## Separation from embedding-based document retrieval

MemCode user memory is not a replacement for an embedding store:

| | Embedding store (RAG) | MemCode user memory (this example) |
|---|---|---|
| What is stored | Document chunks you ingest as operator content | Extracted facts and events about an individual user |
| Who writes | Your ingestion pipeline | Explicit, user-approved application actions only |
| Scope | The knowledge base of the application | One memory owner per MemCode credential, bound server-side |
| Lifecycle | You re-embed and re-index documents | MemCode classifies, extracts and maintains entries over time |
| Retrieval | Vector similarity over chunks | Semantic search and attributed retrieval over memory domains |

Typical combination: use an embedding store to ground answers in your documents, use
MemCode to recall the user across sessions, and keep per-session context in `ChatMemory`.

## Files

- `MemCodeClient` — small REST client for the MemCode personal v2 API
  (`POST /v2/memory/ingest`, `GET /v2/memory/ingest/{job_id}/status`,
  `POST /v2/memory/search`, `POST /v2/memory/retrieve`), including error mapping for
  rejected credentials, rate limits and outages.
- `MemoryPrompt` — builds the system prompt and wraps retrieved entries in a delimited
  `<memory>` block whose delimiter lookalikes are neutralized before the model sees them.
- `MemCodeLongTermMemoryExample` — the runnable demo: session 1 stores an approved fact,
  session 2 starts with empty `ChatMemory` and still recalls it from MemCode.

## Setup

```bash
export OPENAI_API_KEY=sk-...            # used by the OpenAiChatModel in the demo
export MEMCODE_API_KEY=mc_live_...      # create it at https://app.memcode.in/dashboard?section=api-keys
# export MEMCODE_API_URL=https://memory.memcode.in   # optional, this is the default
```

The MemCode API key determines the memory owner server-side. Keys are issued per
integration in the MemCode dashboard; requests that try to override identity or
attribution are rejected by the service.

## Run

```bash
mvn -q compile exec:java -Dexec.mainClass=dev.langchain4j.example.memcode.MemCodeLongTermMemoryExample
```

Ingestion is asynchronous: MemCode returns a durable receipt (`job_id`, `status`) and the
example polls `GET /v2/memory/ingest/{job_id}/status` until a terminal state appears. A
queued receipt is not proof of completion, so if session 2 runs before processing finishes
the fact may not be retrievable yet.

## Tests

```bash
mvn test
```

The tests run a local HTTP stub (no credentials needed) and verify request shaping
(Bearer credential, bounded `top_k`, body fields), response parsing, error mapping and the
neutralization of delimiter lookalikes in retrieved content.

## Guardrails demonstrated

- Retrieved memory is untrusted data, never instructions: it is delimited, sanitized and
  the system prompt states that the current user message wins on conflict.
- Writes require an explicit, user-confirmed application action (`rememberApproved`).
- Secrets, transcripts and model output are never stored by the example.
- If MemCode is unavailable, the conversation continues without long-term memory.
