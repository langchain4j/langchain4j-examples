import dev.langchain4j.community.store.embedding.valkey.ValkeyEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import glide.api.GlideClient;
import glide.api.models.configuration.GlideClientConfiguration;
import glide.api.models.configuration.NodeAddress;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

public class ValkeyEmbeddingStoreExample {

    public static void main(String[] args) throws Exception {

        // Start a Valkey container with vector search support
        GenericContainer<?> valkey = new GenericContainer<>("valkey/valkey-bundle:latest")
                .withExposedPorts(6379);
        valkey.start();

        // Connect to Valkey using GlideClient
        GlideClientConfiguration config = GlideClientConfiguration.builder()
                .address(NodeAddress.builder()
                        .host(valkey.getHost())
                        .port(valkey.getMappedPort(6379))
                        .build())
                .build();
        GlideClient client = GlideClient.createClient(config).get();

        // Use a local embedding model (384 dimensions, no API key needed)
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // Create the ValkeyEmbeddingStore
        ValkeyEmbeddingStore embeddingStore = ValkeyEmbeddingStore.builder()
                .client(client)
                .dimension(384)
                .indexName("example-index")
                .prefix("example:")
                .build();

        // Ingest documents
        List<TextSegment> docs = List.of(
                TextSegment.from("Valkey is a high-performance in-memory data store."),
                TextSegment.from("Vector search finds similar items by embedding distance."),
                TextSegment.from("HNSW is an algorithm for approximate nearest neighbors."),
                TextSegment.from("Valkey supports JSON documents and full-text search."),
                TextSegment.from("LangChain4j provides a unified API for LLM applications in Java.")
        );

        List<Embedding> embeddings = embeddingModel.embedAll(docs).content();
        List<String> ids = embeddingStore.addAll(embeddings, docs);
        System.out.println("Stored " + ids.size() + " documents");

        // Query
        String query = "How does similarity search work?";
        System.out.println("\nQuery: \"" + query + "\"\n");

        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchResult<TextSegment> results = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(3)
                        .minScore(0.5)
                        .build());

        System.out.println("Results:");
        for (EmbeddingMatch<TextSegment> match : results.matches()) {
            System.out.printf("  %.3f: %s%n", match.score(), match.embedded().text());
        }

        // Cleanup
        embeddingStore.removeAll(ids);
        embeddingStore.close();
        valkey.stop();

        System.out.println("\nDone!");
    }
}
