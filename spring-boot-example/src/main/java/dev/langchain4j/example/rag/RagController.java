package dev.langchain4j.example.rag;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagAssistant assistant;

    public RagController(RagAssistant assistant) {
        this.assistant = assistant;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return assistant.answer(question);
    }
}