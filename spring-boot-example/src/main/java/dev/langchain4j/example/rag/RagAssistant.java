package dev.langchain4j.example.rag;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface RagAssistant {
    String answer(String question);
}