package dev.langchain4j.example.aiservice;

import dev.langchain4j.service.spring.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * This is an example of using an {@link AiService}, a high-level LangChain4j API.
 */
@RestController
class AssistantController {

    Assistant assistant;
    StreamingAssistant streamingAssistant;

    AssistantController(Assistant assistant, StreamingAssistant streamingAssistant) {
        this.assistant = assistant;
        this.streamingAssistant = streamingAssistant;
    }

    @GetMapping("/assistant")
    public String assistant(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return assistant.chat(message);
    }

    @GetMapping("/streamingAssistant")
    public Flux<String> streamingAssistant(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return streamingAssistant.chat(message);
    }
}
