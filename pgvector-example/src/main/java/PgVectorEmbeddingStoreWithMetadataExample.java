
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
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

            segment1 = TextSegment.from("I like basket.", Metadata.metadata("userId", "2"));
            embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();

            Filter onlyForUser1 = metadataKey("userId").isEqualTo("1");
            EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding).filter(onlyForUser1).build();

            EmbeddingSearchResult<TextSegment> embeddingSearchResult = embeddingStore.search(embeddingSearchRequest);

            EmbeddingMatch<TextSegment> embeddingMatch = embeddingSearchResult.matches().get(0);

            System.out.println(embeddingMatch.score());
            System.out.println(embeddingMatch.embedded().text());

            Filter onlyForUser2 = metadataKey("userId").isEqualTo("2");
            embeddingSearchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding).filter(onlyForUser2).build();

            embeddingSearchResult = embeddingStore.search(embeddingSearchRequest);

            embeddingMatch = embeddingSearchResult.matches().get(0);

            System.out.println(embeddingMatch.score());
            System.out.println(embeddingMatch.embedded().text());
            postgreSQLContainer.stop();
        }
    }
}
