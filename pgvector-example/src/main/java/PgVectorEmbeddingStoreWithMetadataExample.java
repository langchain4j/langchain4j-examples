
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

public class PgVectorEmbeddingStoreWithMetadataExample {

    public static void main(String[] args) {

        DockerImageName dockerImageName = DockerImageName.parse("pgvector/pgvector:pg16");
        try (PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(dockerImageName)) {
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

            TextSegment segment1 = TextSegment.from("I like football.", Metadata.metadata("userId", "1"));
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("I like basketball.", Metadata.metadata("userId", "2"));
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();

            // search for user 1

            Filter onlyForUser1 = metadataKey("userId").isEqualTo("1");

            EmbeddingSearchRequest embeddingSearchRequest1 = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .filter(onlyForUser1)
                    .build();

            EmbeddingSearchResult<TextSegment> embeddingSearchResult1 = embeddingStore.search(embeddingSearchRequest1);
            EmbeddingMatch<TextSegment> embeddingMatch1 = embeddingSearchResult1.matches().get(0);

            System.out.println(embeddingMatch1.score());
            System.out.println(embeddingMatch1.embedded().text());

            // search for user 2

            Filter onlyForUser2 = metadataKey("userId").isEqualTo("2");

            EmbeddingSearchRequest embeddingSearchRequest2 = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .filter(onlyForUser2)
                    .build();

            EmbeddingSearchResult<TextSegment> embeddingSearchResult2 = embeddingStore.search(embeddingSearchRequest2);
            EmbeddingMatch<TextSegment> embeddingMatch2 = embeddingSearchResult2.matches().get(0);

            System.out.println(embeddingMatch2.score());
            System.out.println(embeddingMatch2.embedded().text());

            postgreSQLContainer.stop();
        }
    }
}
