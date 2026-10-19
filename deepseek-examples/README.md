# DeepSeek Examples

Examples of using [DeepSeek](https://www.deepseek.com/) models with LangChain4j.

DeepSeek provides an OpenAI-compatible API, so these examples use the
`langchain4j-open-ai` module with `baseUrl("https://api.deepseek.com")`.

## Prerequisites

- Java 17+
- A DeepSeek API key: https://platform.deepseek.com/api_keys

Export your key:

```bash
export DEEPSEEK_API_KEY=sk-...
```

## Examples

| File | Shows |
|---|---|
| `DeepSeekChatModelExamples` | Simple prompt + reasoning model |
| `DeepSeekStreamingChatModelExamples` | Streaming responses |
| `DeepSeekToolsExamples` | Tool (function) calling |

## Running

```bash
mvn compile exec:java -Dexec.mainClass=DeepSeekChatModelExamples
```

Or run any `main` method from your IDE.

## Models

| Model | Notes |
|---|---|
| `deepseek-flash` | General-purpose chat model |
| `deepseek-v4-pro` | Reasoning model (returns chain of thought) |

Pricing and peak/off-peak hours: https://api-docs.deepseek.com/quick_start/pricing
