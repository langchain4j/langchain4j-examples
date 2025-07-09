import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.engine.EmbeddingStoreConfig;
import dev.langchain4j.engine.PostgresEngine;
import dev.langchain4j.engine.MetadataColumn;
import dev.langchain4j.store.embedding.cloudsql.PostgresEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import java.sql.SQLException;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PostgresEmbeddingStoreWithMetadataExample {

        private static final String TABLE_NAME = "my_test_table";
        private static final Integer VECTOR_SIZE = 384;

        public static void main(String[] args) throws SQLException {

                EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
                // Create connection pool
                PostgresEngine engine = new PostgresEngine.Builder()
                                .projectId(System.getenv("PROJECT_ID"))
                                .region(System.getenv("REGION"))
                                .instance(System.getenv("INSTANCE"))
                                .database(System.getenv("DB_NAME"))
                                .user(System.getenv("USER"))
                                .password(System.getenv("PASSWORD"))
                                .ipType("public")
                                .build();

                // Create embedding store table with metadata columns
                List<MetadataColumn> metadataColumns = new ArrayList<>();
                metadataColumns.add(new MetadataColumn("userId", "uuid", true));
                EmbeddingStoreConfig embeddingStoreConfig =
                                new EmbeddingStoreConfig.Builder(TABLE_NAME, VECTOR_SIZE)
                                                .metadataColumns(metadataColumns)
                                                .overwriteExisting(true).storeMetadata(true)
                                                .build();

                engine.initVectorStoreTable(embeddingStoreConfig);

                // Initialize embedding store to use metadata columns
                List<String> metaColumnNames = metadataColumns.stream().map(c -> c.getName())
                                .collect(Collectors.toList());

                PostgresEmbeddingStore embeddingStore =
                                new PostgresEmbeddingStore.Builder(engine, TABLE_NAME)
                                                .metadataColumns(metaColumnNames).build();

                // Add entries to embedding store
                Metadata metadata1 = new Metadata();
                UUID user1 = UUID.randomUUID();
                metadata1.put("userId", user1);
                TextSegment segment1 = TextSegment.from("I like turtles.", metadata1);
                Embedding embedding1 = embeddingModel.embed(segment1).content();
                embeddingStore.add(embedding1, segment1);

                Metadata metadata2 = new Metadata();
                UUID user2 = UUID.randomUUID();
                metadata2.put("userId", user2);
                TextSegment segment2 = TextSegment.from(
                                "I do not like fish. My favorite animal is a horse!", metadata2);
                Embedding embedding2 = embeddingModel.embed(segment2).content();
                embeddingStore.add(embedding2, segment2);

                // Search embedding store
                Embedding queryEmbedding =
                                embeddingModel.embed("What is your favorite animal?").content();
                EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                                .queryEmbedding(queryEmbedding).maxResults(1).build();
                EmbeddingSearchResult<TextSegment> searchResult =
                                embeddingStore.search(searchRequest);

                EmbeddingMatch<TextSegment> embeddingMatch = searchResult.matches().get(0);
                System.out.println("Unfiltered match:");
                System.out.println(embeddingMatch.score());
                System.out.println(embeddingMatch.embedded().text());

                // Search embedding store with filter
                Filter onlyForUser1 = metadataKey("userId").isEqualTo(user1);

                EmbeddingSearchRequest embeddingSearchRequest1 = EmbeddingSearchRequest.builder()
                                .queryEmbedding(queryEmbedding).filter(onlyForUser1).build();

                EmbeddingSearchResult<TextSegment> embeddingSearchResult1 =
                                embeddingStore.search(embeddingSearchRequest1);
                EmbeddingMatch<TextSegment> embeddingMatch1 =
                                embeddingSearchResult1.matches().get(0);
                System.out.println("Filtered match:");
                System.out.println(embeddingMatch1.score());
                System.out.println(embeddingMatch1.embedded().text());

                engine.close();
        }
}
