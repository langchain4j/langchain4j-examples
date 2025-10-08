import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.couchbase.CouchbaseEmbeddingStore;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;

public class CouchbaseEmbeddingSearchExample {

    private static BucketDefinition testBucketDefinition = new BucketDefinition("default")
            .withPrimaryIndex(true)
            .withQuota(100);

    public static void main(String[] args) throws InterruptedException {

        try (CouchbaseContainer couchbase = new CouchbaseContainer(DockerImageName.parse("couchbase:enterprise").asCompatibleSubstituteFor("couchbase/server"))
                .withCredentials("Administrator", "password")
                .withBucket(testBucketDefinition)
                .withStartupTimeout(Duration.ofMinutes(1))) {

            couchbase.start();

            CouchbaseEmbeddingStore embeddingStore = new CouchbaseEmbeddingStore.Builder()
                    .clusterUrl(couchbase.getConnectionString())
                    .username(couchbase.getUsername())
                    .password(couchbase.getPassword())
                    .bucketName(testBucketDefinition.getName())
                    .scopeName("_default")
                    .collectionName("_default")
                    .searchIndexName("test")
                    .dimensions(384)
                    .build();

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            TextSegment segment1 = TextSegment.from("I like football.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            Thread.sleep(1000); // to be sure that embeddings were persisted

            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
            EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(1)
                    .build();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(embeddingSearchRequest).matches();
            EmbeddingMatch<TextSegment> embeddingMatch = matches.get(0);

            System.out.println(embeddingMatch.score()); // 0.81442887
            System.out.println(embeddingMatch.embedded().text()); // I like football.
        }
    }
}
