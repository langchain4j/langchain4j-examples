import dev.langchain4j.community.store.embedding.yugabytedb.YugabyteDBEmbeddingStore;
import dev.langchain4j.community.store.embedding.yugabytedb.YugabyteDBEngine;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

/**
 * This example demonstrates using YugabyteDB with the PostgreSQL JDBC driver.
 *
 * PostgreSQL JDBC driver is recommended for:
 * - Standard SQL operations
 * - Maximum PostgreSQL compatibility
 * - Simple single-node or replicated deployments
 */
public class YugabyteDBWithPostgreSQLDriverExample {

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

            System.out.println("=== Using PostgreSQL JDBC Driver ===");
            System.out.println("Driver: org.postgresql.Driver");
            System.out.println("Best for: Standard SQL operations and PostgreSQL compatibility");
            System.out.println();

            // Create YugabyteDB engine with PostgreSQL JDBC driver
            engine = YugabyteDBEngine.builder()
                    .host(yugabyteContainer.getHost())
                    .port(yugabyteContainer.getMappedPort(5433))
                    .database("yugabyte")
                    .username("yugabyte")
                    .password("yugabyte")
                    .usePostgreSQLDriver(true) // ← Use PostgreSQL JDBC driver
                    .maxPoolSize(10)
                    .build();

            EmbeddingStore<TextSegment> embeddingStore = YugabyteDBEmbeddingStore.builder()
                    .engine(engine)
                    .tableName("postgres_driver_embeddings")
                    .dimension(embeddingModel.dimension())
                    .createTableIfNotExists(true)
                    .build();

            // Add some sample data
            TextSegment segment1 = TextSegment.from("PostgreSQL driver provides excellent compatibility.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("YugabyteDB is PostgreSQL compatible.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            TextSegment segment3 = TextSegment.from("Vector search works seamlessly with pgvector.");
            Embedding embedding3 = embeddingModel.embed(segment3).content();
            embeddingStore.add(embedding3, segment3);

            // Search for similar embeddings
            Embedding queryEmbedding = embeddingModel.embed("Tell me about PostgreSQL compatibility").content();

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(2)
                    .build();

            List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(searchRequest).matches();

            System.out.println("Search Results:");
            for (EmbeddingMatch<TextSegment> match : relevant) {
                System.out.println("  Score: " + String.format("%.4f", match.score()));
                System.out.println("  Text: " + match.embedded().text());
                System.out.println();
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

