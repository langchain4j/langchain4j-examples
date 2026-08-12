package dev.langchain4j.example;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final IngestionService ingestionService;
    private final RagAssistant ragAssistant;

    public DocumentController(IngestionService ingestionService, RagAssistant ragAssistant) {
        this.ingestionService = ingestionService;
        this.ragAssistant = ragAssistant;
    }

    /**
     * Ingests raw text into PGVector.
     * Example: POST /api/documents  { "text": "..." }
     */
    @PostMapping("/documents")
    public IngestResponse ingest(@RequestBody IngestRequest request) {
        ingestionService.ingest(request.text());
        return new IngestResponse("Document ingested successfully");
    }

    /**
     * Answers a question using RAG over previously ingested documents.
     * Example: POST /api/ask  { "question": "..." }
     */
    @PostMapping("/ask")
    public AskResponse ask(@RequestBody AskRequest request) {
        String answer = ragAssistant.answer(request.question());
        return new AskResponse(answer);
    }

    public record IngestRequest(String text) {}
    public record IngestResponse(String message) {}
    public record AskRequest(String question) {}
    public record AskResponse(String answer) {}
}
