# Google AI Gemini Examples

This module contains examples demonstrating how to use the LangChain4j Google AI Gemini integration.

## Prerequisites

- Java 17 or higher
- Maven
- A Google AI Gemini API key

Set your API key as an environment variable:

```bash
export GOOGLE_AI_GEMINI_API_KEY=your-api-key-here
```

## Examples

### Basic Chat

| Example | Description |
|---------|-------------|
| [Example01_SimpleChat](src/main/java/dev/langchain4j/example/gemini/Example01_SimpleChat.java) | Basic chat interaction with Gemini using `GoogleAiGeminiChatModel`. |
| [Example02_StreamingChat](src/main/java/dev/langchain4j/example/gemini/Example02_StreamingChat.java) | Streaming chat responses in real-time with completion and error callbacks. |

### Embeddings

| Example | Description |
|---------|-------------|
| [Example03_SimpleEmbedding](src/main/java/dev/langchain4j/example/gemini/Example03_SimpleEmbedding.java) | Generate embedding vectors from text using `GoogleAiEmbeddingModel`. |
| [Example07_EmbeddingWithTaskTypes](src/main/java/dev/langchain4j/example/gemini/Example07_EmbeddingWithTaskTypes.java) | Configure embeddings for different use cases (retrieval, similarity, classification, clustering). |

### Token Counting

| Example | Description |
|---------|-------------|
| [Example04_TokenCounting](src/main/java/dev/langchain4j/example/gemini/Example04_TokenCounting.java) | Estimate token counts for text and messages using `GoogleAiGeminiTokenCountEstimator`. |

### Structured Output

| Example | Description |
|---------|-------------|
| [Example05_ChatWithJsonResponse](src/main/java/dev/langchain4j/example/gemini/Example05_ChatWithJsonResponse.java) | Generate structured JSON responses using JSON schema constraints. |

### Tools & Function Calling

| Example | Description |
|---------|-------------|
| [Example06_ChatWithTools](src/main/java/dev/langchain4j/example/gemini/Example06_ChatWithTools.java) | Define and use tools with the `@Tool` annotation, handle execution requests and return results. |

### Multimodal

| Example | Description |
|---------|-------------|
| [Example08_FileUpload](src/main/java/dev/langchain4j/example/gemini/Example08_FileUpload.java) | Upload files using `GeminiFiles` API and reference them in chat requests. |
| [Example09_MultimodalChat](src/main/java/dev/langchain4j/example/gemini/Example09_MultimodalChat.java) | Send images alongside text prompts for multimodal interactions. |

### Advanced Features

| Example | Description |
|---------|-------------|
| [Example10_ChatWithThinking](src/main/java/dev/langchain4j/example/gemini/Example10_ChatWithThinking.java) | Enable thinking/reasoning mode with configurable thinking budget for complex problems. |
| [Example11_ChatWithSafetySettings](src/main/java/dev/langchain4j/example/gemini/Example11_ChatWithSafetySettings.java) | Configure safety thresholds for different harm categories. |

### Batch Processing

Batch processing offers **50% cost reduction** compared to interactive requests with a 24-hour turnaround SLO. Ideal for large-scale, non-urgent processing tasks.

| Example | Description |
|---------|-------------|
| [Example12_BatchChatInline](src/main/java/dev/langchain4j/example/gemini/Example12_BatchChatInline.java) | Inline batch chat processing—submit multiple chat requests in a single batch job. |
| [Example13_BatchEmbeddingInline](src/main/java/dev/langchain4j/example/gemini/Example13_BatchEmbeddingInline.java) | Inline batch embedding—generate embeddings for multiple text segments in a single batch. |
| [Example14_BatchChatFromFile](src/main/java/dev/langchain4j/example/gemini/Example14_BatchChatFromFile.java) | File-based batch chat—write requests to JSONL, upload via Files API, and process as a batch. |
| [Example15_BatchEmbedFromFile](src/main/java/dev/langchain4j/example/gemini/Example15_BatchEmbedFromFile.java) | File-based batch embedding—for large embedding batches exceeding the 20MB inline limit. |

## Running the Examples

Run any example using Maven:

```bash
GOOGLE_AI_GEMINI_API_KEY=your-api-key-here mvn compile exec:java -Dexec.mainClass="dev.langchain4j.example.gemini.Example01_SimpleChat"
```

Or if you've already exported the environment variable:

```bash
mvn compile exec:java -Dexec.mainClass="dev.langchain4j.example.gemini.Example01_SimpleChat"
```

You can also run directly from your IDE by executing the `main` method of any example class.

## Batch Processing Overview

### Inline vs File-Based Batching

| Approach | Use Case | Limit |
|----------|----------|-------|
| **Inline** | Small to medium batches | Up to 20MB of requests |
| **File-based** | Large batches | Limited by Files API (2GB per file) |

### Batch Workflow

1. **Create** a batch job (inline or from file)
2. **Poll** for completion using `retrieveBatchResults()`
3. **Process** results when `BatchSuccess` is returned
4. **Clean up** by deleting the batch job and any uploaded files (Optional as batches are removed after )

### Batch States

| State | Description |
|-------|-------------|
| `BATCH_STATE_PENDING` | Job is queued |
| `BATCH_STATE_RUNNING` | Job is processing |
| `BATCH_STATE_SUCCEEDED` | Job completed successfully |
| `BATCH_STATE_FAILED` | Job failed |
| `BATCH_STATE_CANCELLED` | Job was cancelled |

## Models

### Chat Models

- `gemini-3-pro` - Most capable model for complex reasoning
- `gemini-3-flash` - Fast, efficient model for most tasks
- `gemini-2.5-flash-lite` - Lightweight version for cost-sensitive applications

### Embedding Models

- `gemini-embedding-001` - Latest embedding model with improved performance

## Resources

- [Google AI Gemini Documentation](https://ai.google.dev/docs)
- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [Gemini API Reference](https://ai.google.dev/api/rest)