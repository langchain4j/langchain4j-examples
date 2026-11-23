import dev.langchain4j.community.store.embedding.valkey.ValkeyEmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;
import glide.api.GlideClient;
import glide.api.models.commands.FT.FTCreateOptions.FieldInfo;
import glide.api.models.commands.FT.FTCreateOptions.NumericField;
import glide.api.models.commands.FT.FTCreateOptions.TagField;
import glide.api.models.configuration.GlideClientConfiguration;
import glide.api.models.configuration.NodeAddress;
import org.testcontainers.containers.GenericContainer;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * Demonstrates semantic search with metadata filtering using Valkey.
 *
 * <p>Shows how to store documents with typed metadata (TAG, NUMERIC fields),
 * then combine vector similarity with metadata filters for precise retrieval.</p>
 */
public class ValkeyMetadataFilteringExample {

    public static void main(String[] args) throws Exception {

        // Start a Valkey container with vector search support
        GenericContainer<?> valkey = new GenericContainer<>("valkey/valkey-bundle:latest")
                .withExposedPorts(6379);
        valkey.start();

        // Connect to Valkey
        GlideClient client = GlideClient.createClient(GlideClientConfiguration.builder()
                        .address(NodeAddress.builder()
                                .host(valkey.getHost())
                                .port(valkey.getMappedPort(6379))
                                .build())
                        .build())
                .get();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        // Create store with typed metadata fields
        Map<String, FieldInfo> metadataConfig = Map.of(
                "category", new FieldInfo("$.category", "category", new TagField(',', true)),
                "year", new FieldInfo("$.year", "year", new NumericField())
        );

        ValkeyEmbeddingStore store = ValkeyEmbeddingStore.builder()
                .client(client)
                .dimension(384)
                .indexName("filtered-docs")
                .prefix("filtered:")
                .metadataConfig(metadataConfig)
                .build();

        // Ingest documents with metadata
        List<TextSegment> docs = List.of(
                TextSegment.from("Use TLS encryption for all Valkey connections in production.",
                        Metadata.from(Map.of("category", "security", "year", 2025))),
                TextSegment.from("HNSW indexes trade memory for faster approximate search.",
                        Metadata.from(Map.of("category", "performance", "year", 2024))),
                TextSegment.from("Set maxmemory-policy to allkeys-lru for cache workloads.",
                        Metadata.from(Map.of("category", "operations", "year", 2025))),
                TextSegment.from("Enable AUTH and ACL for multi-tenant deployments.",
                        Metadata.from(Map.of("category", "security", "year", 2024))),
                TextSegment.from("Use connection pooling to maximize throughput.",
                        Metadata.from(Map.of("category", "performance", "year", 2025)))
        );

        List<Embedding> embeddings = embeddingModel.embedAll(docs).content();
        List<String> ids = store.addAll(embeddings, docs);
        System.out.println("Stored " + ids.size() + " documents with metadata\n");

        // Search with TAG filter: only "security" category
        System.out.println("--- Filter: category = 'security' ---");
        Filter securityFilter = metadataKey("category").isEqualTo("security");
        Embedding query1 = embeddingModel.embed("encryption best practices").content();
        EmbeddingSearchResult<TextSegment> results1 = store.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(query1)
                        .maxResults(5)
                        .filter(securityFilter)
                        .build());
        printResults(results1);

        // Search with NUMERIC filter: year >= 2025
        System.out.println("--- Filter: year >= 2025 ---");
        Filter recentFilter = metadataKey("year").isGreaterThanOrEqualTo(2025);
        Embedding query2 = embeddingModel.embed("performance optimization").content();
        EmbeddingSearchResult<TextSegment> results2 = store.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(query2)
                        .maxResults(5)
                        .filter(recentFilter)
                        .build());
        printResults(results2);

        // Search with combined AND filter: security docs from 2025+
        System.out.println("--- Filter: category = 'security' AND year >= 2025 ---");
        Filter combined = metadataKey("category").isEqualTo("security")
                .and(metadataKey("year").isGreaterThanOrEqualTo(2025));
        Embedding query3 = embeddingModel.embed("secure connections").content();
        EmbeddingSearchResult<TextSegment> results3 = store.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(query3)
                        .maxResults(5)
                        .filter(combined)
                        .build());
        printResults(results3);

        // Search with OR filter: security OR performance
        System.out.println("--- Filter: category = 'security' OR category = 'performance' ---");
        Filter either = metadataKey("category").isEqualTo("security")
                .or(metadataKey("category").isEqualTo("performance"));
        Embedding query4 = embeddingModel.embed("Valkey best practices").content();
        EmbeddingSearchResult<TextSegment> results4 = store.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(query4)
                        .maxResults(5)
                        .filter(either)
                        .build());
        printResults(results4);

        // Remove by filter: delete all 2024 docs
        System.out.println("--- Removing documents where year < 2025 ---");
        Filter oldDocs = metadataKey("year").isLessThan(2025);
        store.removeAll(oldDocs);
        System.out.println("Removed old documents\n");

        // Verify removal
        System.out.println("--- Searching all remaining docs ---");
        Embedding query5 = embeddingModel.embed("Valkey").content();
        EmbeddingSearchResult<TextSegment> remaining = store.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(query5)
                        .maxResults(10)
                        .build());
        printResults(remaining);

        // Cleanup
        store.removeAll(ids);
        store.close();
        valkey.stop();

        System.out.println("Done!");
    }

    private static void printResults(EmbeddingSearchResult<TextSegment> results) {
        if (results.matches().isEmpty()) {
            System.out.println("  (no results)\n");
            return;
        }
        for (EmbeddingMatch<TextSegment> match : results.matches()) {
            System.out.printf("  %.3f: %s %s%n",
                    match.score(),
                    match.embedded().text(),
                    match.embedded().metadata().toMap());
        }
        System.out.println();
    }
}
