import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ElasticsearchEmbeddingStoreExample {

    private static final Logger LOGGER = LogManager.getLogger(ElasticsearchEmbeddingStoreExample.class);

    /**
     * To run this example, ensure you have Elasticsearch running locally. If not, then:
     * - Execute "docker pull docker.elastic.co/elasticsearch/elasticsearch:8.9.0"
     * - Execute "docker run -d -p 9200:9200 -p 9300:9300 -e discovery.type=single-node -e xpack.security.enabled=false docker.elastic.co/elasticsearch/elasticsearch:8.9.0"
     * - Wait until Elasticsearch is ready to serve (may take a few minutes)
     */
    public static void main(String[] args) {
        DockerImageName imageName = DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.9.0");
        Map<String, String> env = new HashMap<>();
        env.put("xpack.security.enabled", "false");
        env.put("discovery.type", "single-node");

        try (ElasticsearchContainer elastic = new ElasticsearchContainer(imageName).withCertPath(null).withEnv(env)) {
            elastic.start();

            EmbeddingStore<TextSegment> embeddingStore = ElasticsearchEmbeddingStore.builder()
                    .serverUrl(elastic.getHttpHostAddress())
                    .dimension(384)
                    .build();

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            TextSegment segment1 = TextSegment.from("I like football.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            // to be sure that embeddings were persisted
            TimeUnit.MILLISECONDS.sleep(1000);

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
