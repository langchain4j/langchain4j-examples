package dev.langchain4j.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
/**
 * Wires PGVector as the embedding store for a Retrieval-Augmented Generation (RAG) pipeline.
 *
 * <p>NOTE: there is currently no {@code langchain4j-pgvector-spring-boot-starter}
 * (tracked at <a href="https://github.com/langchain4j/langchain4j/issues/2102">#2102</a>),
 * so {@link PgVectorEmbeddingStore} is wired manually here as a {@code @Bean}, using
 * a pooled {@link HikariDataSource} rather than individual host/port parameters
 * (recommended for production use per the LangChain4j docs).
 *
 * <p>{@link EmbeddingModel} and {@link ChatModel} beans are auto-configured by
 * {@code langchain4j-open-ai-spring-boot-starter} from the {@code langchain4j.open-ai.*}
 * properties in {@code application.yml} â€” they don't need to be created here.
 */
@Configuration
public class RagConfiguration {

    @Value("${pgvector.datasource.url}")
    private String jdbcUrl;

    @Value("${pgvector.datasource.username}")
    private String username;

    @Value("${pgvector.datasource.password}")
    private String password;

    @Value("${pgvector.table:document_embeddings}")
    private String table;

    @Value("${langchain4j.ollama.embedding-model.base-url}")
    private String ollamaEmbeddingBaseUrl;

    @Value("${langchain4j.ollama.embedding-model.model-name}")
    private String ollamaEmbeddingModelName;

    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(ollamaEmbeddingBaseUrl)
                .modelName(ollamaEmbeddingModelName)
                .build();
    }

    @Bean
    public HikariDataSource pgVectorDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(HikariDataSource pgVectorDataSource,
                                                        EmbeddingModel embeddingModel) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(pgVectorDataSource)
                .table(table)
                .dimension(embeddingModel.dimension())
                .createTable(true)
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                              EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .build();
    }

    @Bean
    public RagAssistant ragAssistant(ChatModel chatModel, ContentRetriever contentRetriever) {
        return AiServices.builder(RagAssistant.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .build();
    }
}
