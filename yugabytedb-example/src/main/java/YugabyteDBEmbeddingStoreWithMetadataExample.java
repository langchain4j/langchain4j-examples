import dev.langchain4j.community.store.embedding.yugabytedb.DefaultMetadataStorageConfig;
import dev.langchain4j.community.store.embedding.yugabytedb.MetadataStorageConfig;
import dev.langchain4j.community.store.embedding.yugabytedb.MetadataStorageMode;
import dev.langchain4j.community.store.embedding.yugabytedb.YugabyteDBEmbeddingStore;
import dev.langchain4j.community.store.embedding.yugabytedb.YugabyteDBEngine;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

public class YugabyteDBEmbeddingStoreWithMetadataExample {

    public static void main(String[] args) {
        GenericContainer<?> yugabyteContainer = null;
        YugabyteDBEngine engine = null;

        try {
            DockerImageName dockerImageName = DockerImageName.parse("yugabytedb/yugabyte:2025.1.0.1-b3");
            yugabyteContainer = new GenericContainer<>(dockerImageName)
                    .withExposedPorts(5433, 7000, 9000, 15433, 9042)
                    .withCommand("bin/yugabyted", "start", "--background=false")
                    .waitingFor(Wait.forListeningPorts(5433).withStartupTimeout(Duration.ofMinutes(5)));
            
            yugabyteContainer.start();

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            // Create YugabyteDB engine with PostgreSQL driver
            engine = YugabyteDBEngine.builder()
                    .host(yugabyteContainer.getHost())
                    .port(yugabyteContainer.getMappedPort(5433))
                    .database("yugabyte")
                    .username("yugabyte")
                    .password("yugabyte")
                    .usePostgreSQLDriver(true)
                    .build();

            // Configure metadata storage (JSONB format)
            MetadataStorageConfig metadataConfig = DefaultMetadataStorageConfig.builder()
                    .storageMode(MetadataStorageMode.COMBINED_JSONB)
                    .build();

            EmbeddingStore<TextSegment> embeddingStore = YugabyteDBEmbeddingStore.builder()
                    .engine(engine)
                    .tableName("test_embeddings_with_metadata")
                    .dimension(embeddingModel.dimension())
                    .metadataStorageConfig(metadataConfig)
                    .createTableIfNotExists(true)
                    .build();

            // Add embeddings with metadata
            TextSegment segment1 = TextSegment.from("I like football.", 
                    Metadata.from("category", "sports").put("user", "john"));
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.",
                    Metadata.from("category", "weather").put("user", "alice"));
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            TextSegment segment3 = TextSegment.from("I love basketball.",
                    Metadata.from("category", "sports").put("user", "bob"));
            Embedding embedding3 = embeddingModel.embed(segment3).content();
            embeddingStore.add(embedding3, segment3);

            // Search with metadata filter
            Embedding queryEmbedding = embeddingModel.embed("What sport do you like?").content();

            Filter categoryFilter = new IsEqualTo("category", "sports");

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(5)
                    .filter(categoryFilter)
                    .build();

            List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(searchRequest).matches();

            System.out.println("Found " + relevant.size() + " sports-related results:");
            for (EmbeddingMatch<TextSegment> match : relevant) {
                System.out.println("Score: " + match.score());
                System.out.println("Text: " + match.embedded().text());
                System.out.println("Metadata: " + match.embedded().metadata());
                System.out.println("---");
            }

            System.out.println("\n✅ Example completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error running example: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Give Testcontainers time to cleanup gracefully
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
            
            // Cleanup resources
            System.out.println("🧹 Cleaning up resources...");
            if (engine != null) {
                try {
                    engine.close();
                } catch (Exception e) {
                    System.err.println("Error closing engine: " + e.getMessage());
                }
            }
            if (yugabyteContainer != null) {
                try {
                    yugabyteContainer.stop();
                } catch (Exception e) {
                    System.err.println("Error stopping container: " + e.getMessage());
                }
            }
        }
    }
}

