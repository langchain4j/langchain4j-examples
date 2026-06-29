import com.hazelcast.config.Config;
import com.hazelcast.config.vector.Metric;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import dev.langchain4j.community.store.embedding.hazelcast.HazelcastEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.util.List;

/**
 * Stores embeddings in a Hazelcast {@code VectorCollection} and runs a similarity search.
 * <p>
 * Requires Hazelcast Enterprise (a valid {@code HZ_LICENSEKEY}). An embedded single-member
 * cluster is used here; no external process is needed.
 */
public class HazelcastEmbeddingStoreExample {

    public static void main(String[] args) {
        Config config = new Config();
        config.setLicenseKey(System.getenv("HZ_LICENSEKEY"));
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        try {
            EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

            EmbeddingStore<TextSegment> store = HazelcastEmbeddingStore.builder()
                    .hazelcastInstance(hz)
                    .collectionName("embeddings") // optional, defaults to "embeddings"
                    .dimension(384) // must match the embedding model
                    .metric(Metric.COSINE) // optional, defaults to COSINE
                    .build();

            TextSegment segment1 = TextSegment.from("I like football.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            store.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            store.add(embedding2, segment2);

            Embedding queryEmbedding =
                    embeddingModel.embed("What is your favourite sport?").content();
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(1)
                    .build();

            EmbeddingSearchResult<TextSegment> result = store.search(request);
            List<EmbeddingMatch<TextSegment>> matches = result.matches();
            matches.forEach(match -> System.out.println(match.score() + " : " + match.embedded().text()));
        } finally {
            hz.shutdown();
        }
    }
}
