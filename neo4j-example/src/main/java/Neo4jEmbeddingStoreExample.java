import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.neo4j.Neo4jEmbeddingStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

public class Neo4jEmbeddingStoreExample {

    private static final Logger LOGGER = LogManager.getLogger(Neo4jEmbeddingStoreExample.class);

    /**
     * To run this example, ensure you have Neo4j running locally,
     * and change uri, username and password strings consistently.
     * If not, then:
     * - Execute "docker pull neo4j:latest"
     * - Execute "docker run -d -p 7687:7687 --env NEO4J_AUTH=neo4j/password1234 neo4j:latest"
     * - Wait until Neo4j is ready to serve (may take a few minutes)
     */

    public static void main(String[] args) {
        String username = "neo4j";
        String password = "password1234";

        try (Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>(DockerImageName.parse("neo4j:latest"))) {
            neo4jContainer.withEnv("NEO4J_AUTH", username + "/" + password).start();
            EmbeddingStore<TextSegment> embeddingStore = Neo4jEmbeddingStore.builder()
                    .driver(GraphDatabase.driver(neo4jContainer.getBoltUrl(), AuthTokens.basic(username, password)))
                    .dimension(384)
                    .build();

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            TextSegment segment1 = TextSegment.from("I like football.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
            List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
            EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

            // expected 0.8144288659095
            LOGGER.info("Score: {}", embeddingMatch.score());
            // expected "I like football."
            LOGGER.info("Embedded: {}", embeddingMatch.embedded().text());
        } catch (Exception e) {
            LOGGER.error("Error: {}", e.getMessage());
        }
    }
}
