package dev.langchain4j.example.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG wiring for the Spring Boot example.
 * <p>
 * The {@link EmbeddingModel} bean is provided by
 * {@code langchain4j-open-ai-spring-boot-starter} via the
 * {@code langchain4j.open-ai.embedding-model.*} properties — we only need to
 * define an in-memory store and a content retriever on top of it. Document
 * ingestion happens at startup in {@link RagIngestor}.
 * <p>
 * For production workloads, replace {@link InMemoryEmbeddingStore} with a
 * persistent store such as PgVector, Qdrant, Pinecone, Milvus, or Elasticsearch;
 * the rest of the wiring stays the same.
 */
@Configuration
public class RagConfiguration {

    /** Minimum cosine similarity for a chunk to be considered relevant. */
    private static final double MIN_SCORE = 0.6;

    /** Maximum number of relevant chunks injected into the prompt per query. */
    private static final int MAX_RESULTS = 3;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                             EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(MAX_RESULTS)
                .minScore(MIN_SCORE)
                .build();
    }
}

