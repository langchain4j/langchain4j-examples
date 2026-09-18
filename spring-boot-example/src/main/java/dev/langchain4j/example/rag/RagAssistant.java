package dev.langchain4j.example.rag;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * Declarative AI Service with Retrieval-Augmented Generation enabled.
 * <p>
 * The {@code ContentRetriever} bean defined in {@link RagConfiguration} is auto-wired
 * by the langchain4j Spring Boot starter, so every call to {@link #chat(String)} is
 * grounded in the documents ingested at startup by {@link RagIngestor}.
 */
@AiService
public interface RagAssistant {

    @SystemMessage("""
            You are a helpful assistant. Answer the user's question strictly
            using the provided context. If the answer is not in the context,
            reply: "I don't know based on the provided documents."
            """)
    String chat(String userMessage);
}

