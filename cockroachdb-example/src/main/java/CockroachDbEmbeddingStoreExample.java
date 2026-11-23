import dev.langchain4j.community.store.embedding.cockroachdb.CockroachDbEmbeddingStore;
import dev.langchain4j.community.store.embedding.cockroachdb.CockroachDbEngine;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.util.List;
import org.testcontainers.containers.CockroachContainer;

/**
 * Minimal RAG example backed by CockroachDB.
 *
 * <p>Uses the Testcontainers cockroachdb/cockroach image and the default
 * sequential-scan index so the example runs without any extra cluster setup.
 * To switch to the C-SPANN distributed ANN index on CockroachDB v25.2 or
 * later, enable the feature flag once per cluster:
 *
 * <pre>
 *   SET CLUSTER SETTING feature.vector_index.enabled = true;
 * </pre>
 *
 * <p>and pass {@code CSpannIndex.builder().build()} to the store via
 * {@code .vectorIndex(...)}.
 */
public class CockroachDbEmbeddingStoreExample {

    public static void main(String[] args) {
        try (CockroachContainer cockroach = new CockroachContainer("cockroachdb/cockroach:latest-v25.2")) {
            cockroach.start();

            CockroachDbEngine engine = CockroachDbEngine.builder()
                    .connectionString(cockroach.getJdbcUrl())
                    .username(cockroach.getUsername())
                    .password(cockroach.getPassword())
                    .build();

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            EmbeddingStore<TextSegment> embeddingStore = CockroachDbEmbeddingStore.builder()
                    .engine(engine)
                    .dimension(embeddingModel.dimension())
                    .tableName("demo_embeddings")
                    .build();

            TextSegment segment1 = TextSegment.from("I like football.");
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("The weather is good today.");
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(1)
                    .build();

            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();
            EmbeddingMatch<TextSegment> match = matches.get(0);

            System.out.println(match.score()); // ~0.81
            System.out.println(match.embedded().text()); // I like football.

            engine.close();
        }
    }
}
