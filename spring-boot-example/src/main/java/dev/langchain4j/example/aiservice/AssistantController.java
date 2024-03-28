package dev.langchain4j.example.aiservice;

import dev.langchain4j.service.spring.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is an example of using an {@link AiService}, a high-level LangChain4j API.
 */
@RestController
class AssistantController {

    Assistant assistant;

    AssistantController(Assistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/assistant")
    public String assistant(@RequestParam(value = "message", defaultValue = "What is the time now?") String message) {
        return assistant.chat(message);
    }
}
