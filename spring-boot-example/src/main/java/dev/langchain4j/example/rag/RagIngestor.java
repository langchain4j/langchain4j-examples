package dev.langchain4j.example.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Ingests {@code documents/rag-sample.txt} from the classpath into the
 * in-memory {@link EmbeddingStore} at application startup so the
 * {@code ContentRetriever} has something to retrieve.
 */
@Component
public class RagIngestor implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RagIngestor.class);

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final Resource sampleDocument;

    public RagIngestor(EmbeddingStore<TextSegment> embeddingStore,
                       EmbeddingModel embeddingModel,
                       @Value("classpath:documents/rag-sample.txt") Resource sampleDocument) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.sampleDocument = sampleDocument;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        DocumentParser parser = new TextDocumentParser();
        try (InputStream in = sampleDocument.getInputStream()) {
            Document document = parser.parse(in);

            EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(300, 30))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build()
                    .ingest(document);

            log.info("Ingested sample document '{}' into the in-memory embedding store.",
                    sampleDocument.getFilename());
        }
    }
}

