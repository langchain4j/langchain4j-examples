
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

import java.util.List;

public class PGVectorEmbeddingStoreExample {

    public static void main(String[] args) {

        PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
                DockerImageName.parse("ankane/pgvector:v0.5.1").asCompatibleSubstituteFor("postgres"));
                // or
                // DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"));
        postgreSQLContainer.start();

        EmbeddingStore<TextSegment> embeddingStore = PgVectorEmbeddingStore.builder()
                .host(postgreSQLContainer.getHost())
                .port(postgreSQLContainer.getFirstMappedPort())
                .database(postgreSQLContainer.getDatabaseName())
                .user(postgreSQLContainer.getUsername())
                .password(postgreSQLContainer.getPassword())
                .table("test")
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

        System.out.println(embeddingMatch.score()); // 0.8144288659095
        System.out.println(embeddingMatch.embedded().text()); // I like football.

        postgreSQLContainer.stop();
    }
}
