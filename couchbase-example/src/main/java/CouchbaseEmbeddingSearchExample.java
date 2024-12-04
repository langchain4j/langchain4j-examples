import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.List;

public class CouchbaseEmbeddingSearchExample {

    public static void main(String[] args) throws InterruptedException {

        try (CouchbaseContainer couchbase = new CouchbaseContainer(DockerImageName.parse("couchbase:enterprise").asCompatibleSubstituteFor("couchbase/server"))
                .withCredentials("Administrator", "password")
                .withBucket(testBucketDefinition)
                .withStartupTimeout(Duration.ofMinutes(1))) {

            couchbase.start();

            CouchbaseEmbeddingStore embeddingStore = new CouchbaseEmbeddingStore.Builder(couchbaseContainer.getConnectionString())
                    .username(couchbaseContainer.getUsername())
                    .password(couchbaseContainer.getPassword())
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
            List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(queryEmbedding, 1);
            EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(0);

            System.out.println(embeddingMatch.score()); // 0.81442887
            System.out.println(embeddingMatch.embedded().text()); // I like football.
        }
    }
}
