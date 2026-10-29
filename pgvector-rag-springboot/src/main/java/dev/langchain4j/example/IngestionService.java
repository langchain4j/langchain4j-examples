package dev.langchain4j.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.stereotype.Service;

/**
 * Splits incoming text into chunks, embeds them, and stores the embeddings in PGVector.
 * This is the "indexing stage" of the RAG pipeline (offline / on-demand),
 * as opposed to {@link RagAssistant}, which handles the "retrieval stage" (online).
 */
@Service
public class IngestionService {

    private final EmbeddingStoreIngestor ingestor;

    public IngestionService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        DocumentSplitter splitter = DocumentSplitters.recursive(300, 50);
        this.ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    public void ingest(String text) {
        ingestor.ingest(Document.from(text));
    }
}
