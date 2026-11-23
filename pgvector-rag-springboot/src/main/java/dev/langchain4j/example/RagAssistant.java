package dev.langchain4j.example;

/**
 * AI Service backed by RAG over documents stored in PGVector.
 * The concrete implementation is created by {@link RagConfiguration#ragAssistant}
 * via {@code AiServices.builder(...)}, with a {@code ContentRetriever} attached
 * so every call is automatically augmented with relevant retrieved context.
 */
public interface RagAssistant {

    String answer(String question);
}
