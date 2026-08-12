# PGVector RAG Spring Boot Example

A Spring Boot application demonstrating Retrieval-Augmented Generation (RAG) backed by
[PGVector](https://github.com/pgvector/pgvector) as the embedding store, with
[Ollama](https://ollama.ai) providing both the chat and embedding models -- fully local,
no API key required.

This fills a gap referenced in the
[LangChain4j PGVector docs](https://docs.langchain4j.dev/integrations/embedding-stores/pgvector/#spring-boot-integration),
which link to this example, but at the time of writing it didn't exist yet
(see [langchain4j/langchain4j#5929](https://github.com/langchain4j/langchain4j/issues/5929)).

> **Note:** There is currently no `langchain4j-pgvector-spring-boot-starter`
> (tracked at [langchain4j/langchain4j#2102](https://github.com/langchain4j/langchain4j/issues/2102)).
> This example manually wires `PgVectorEmbeddingStore` as a Spring `@Bean` in
> [`RagConfiguration`](src/main/java/dev/langchain4j/example/RagConfiguration.java)
> using a pooled `HikariDataSource`, until that starter exists.

## What this demonstrates

- Wiring `PgVectorEmbeddingStore` as a Spring bean with HikariCP connection pooling
- Auto-configured `ChatModel` via `langchain4j-ollama-spring-boot-starter`
- A manually-wired `EmbeddingModel` bean using Ollama's `nomic-embed-text`
- Document ingestion (splitting, embedding, storing) via a REST endpoint
- Question-answering via RAG (`AiServices` + `ContentRetriever`) via a REST endpoint
- Local PGVector setup via Docker Compose

## Prerequisites

- Java 17+
- Docker (for running PGVector locally)
- [Ollama](https://ollama.ai) installed and running locally

## Setup

1. Start PGVector:
```bash
   docker compose up -d
```

2. Install and start Ollama (if you haven't already):
```bash
   brew install ollama
   brew services start ollama
```

3. Pull the models this example uses:
```bash
   ollama pull llama3.2
   ollama pull nomic-embed-text
```

4. Run the application:
```bash
   mvn spring-boot:run
```

## Usage

**Ingest a document:**
```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{"text": "LangChain4j is a Java library for building LLM-powered applications."}'
```

**Ask a question:**
```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is LangChain4j?"}'
```

The first `/ask` call will take longer than subsequent ones, since Ollama needs to load
`llama3.2` into memory before it can generate a response.

## Why Ollama instead of OpenAI?

This example uses Ollama for both the chat and embedding models so it can be run and
verified by anyone without needing an OpenAI API key or billing account. If you'd
prefer to use OpenAI instead, swap `langchain4j-ollama-spring-boot-starter` for
`langchain4j-open-ai-spring-boot-starter` in `pom.xml`, update the `langchain4j.*`
properties in `application.yml` accordingly, and replace the `OllamaEmbeddingModel`
bean in `RagConfiguration` with an `OpenAiEmbeddingModel`.

## Verification

This example was manually verified end-to-end: a document was ingested via
`POST /api/documents`, then a question was asked via `POST /api/ask`, and the response
was grounded in the ingested content (confirming retrieval was actually feeding
context into the model, not just answering from general knowledge).

Note: `llama3.2` is a small (3B parameter) model. In manual testing, it sometimes
ignored the retrieved context and answered from general knowledge instead, even
though retrieval itself worked correctly (confirmed via `log-requests: true` --
the retrieved context was present in the prompt sent to Ollama). This is a known
limitation of small local models rather than an issue with the RAG pipeline. For
more reliable context-following, swap in a larger Ollama model (e.g. `llama3.1:8b`
or `qwen2.5:7b`) or use OpenAI as described above.

An automated integration test using Testcontainers (`PostgreSQLContainer` for pgvector)
was attempted, but was dropped due to a compatibility issue between the Testcontainers
library and newer Docker Engine versions unrelated to this example's code. Manual
verification steps above can be used to confirm the example works correctly.

## Project structure

```
pgvector-rag-springboot/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/dev/langchain4j/example/
        │   ├── PgVectorRagSpringBootApplication.java
        │   ├── RagConfiguration.java
        │   ├── RagAssistant.java
        │   ├── IngestionService.java
        │   └── DocumentController.java
        └── resources/
            └── application.yml
```