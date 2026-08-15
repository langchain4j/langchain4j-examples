package dev.langchain4j.example.rag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal REST surface for the RAG example.
 * <p>
 * Try it with:
 * <pre>{@code
 *   curl "http://localhost:8082/rag/chat?message=What+is+LangChain4j?"
 * }</pre>
 */
@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagAssistant assistant;

    public RagController(RagAssistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message",
            defaultValue = "What is LangChain4j?") String message) {
        return assistant.chat(message);
    }
}

