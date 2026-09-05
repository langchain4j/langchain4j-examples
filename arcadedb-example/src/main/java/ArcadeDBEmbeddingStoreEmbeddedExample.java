import dev.langchain4j.community.store.embedding.arcadedb.ArcadeDBEmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import dev.langchain4j.store.embedding.filter.logical.And;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Example demonstrating the ArcadeDB embedding store in <b>embedded</b> mode, where ArcadeDB
 * runs in-process inside the same JVM — no separate server or Docker container required.
 *
 * <p>The database is stored on the local filesystem at a path you provide.  The store is opened
 * (or created, if it does not yet exist) automatically.  Always call {@link ArcadeDBEmbeddingStore#close()}
 * when you are finished to release resources.
 *
 * <p>For usage against a remote ArcadeDB server, see {@link ArcadeDBEmbeddingStoreRemoteExample}.
 *
 * <p>This example demonstrates:
 * <ul>
 *   <li>Basic embedded store usage (HNSW index, COSINE similarity)</li>
 *   <li>Metadata-enriched embeddings and metadata filtering</li>
 *   <li>Persistent reuse of an existing database directory</li>
 * </ul>
 */
public class ArcadeDBEmbeddingStoreEmbeddedExample {

    private static final String TEST_DOCUMENT = "test-document.txt";

    public static void main(String[] args) throws Exception {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

        List<String> lines = readLinesFromResource(TEST_DOCUMENT);
        System.out.println("Read " + lines.size() + " lines from " + TEST_DOCUMENT);
        System.out.println();

        // Example 1: basic embedded store — database is created in a temp directory
        Path dbPath1 = Files.createTempDirectory("arcadedb-example-basic");
        System.out.println("Database path: " + dbPath1);

        ArcadeDBEmbeddingStore basicStore = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath1.toString())
                .dimension(384)
                .maxConnections(16)
                .beamWidth(100)
                .build();

        try {
            runBasicSearch(basicStore, embeddingModel, lines, "ArcadeDB Embedded (basic)");
        } finally {
            basicStore.close();
        }

        // Example 2: embedded store with metadata filtering
        Path dbPath2 = Files.createTempDirectory("arcadedb-example-filtering");
        System.out.println("Database path: " + dbPath2);

        ArcadeDBEmbeddingStore filteringStore = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath2.toString())
                .dimension(384)
                .build();

        try {
            runMetadataFilteringExample(filteringStore, embeddingModel);
        } finally {
            filteringStore.close();
        }

        // Example 3: reopen a persistent database — same databasePath, data is still there
        Path dbPath3 = Files.createTempDirectory("arcadedb-example-persistent");
        System.out.println("Database path: " + dbPath3);

        runPersistenceExample(dbPath3, embeddingModel);
    }

    /**
     * Adds all lines from the test document to the store and runs random semantic queries.
     */
    private static void runBasicSearch(EmbeddingStore<TextSegment> store,
                                       EmbeddingModel embeddingModel,
                                       List<String> lines,
                                       String storeName) {

        long startTime = System.currentTimeMillis();
        System.out.println("=== Running with store: " + storeName + " ===");
        System.out.println("Adding embeddings...");

        int added = 0;
        for (String line : lines) {
            if (!line.isBlank()) {
                TextSegment segment = TextSegment.from(line);
                Embedding embedding = embeddingModel.embed(segment).content();
                store.add(embedding, segment);
                added++;
            }
        }
        System.out.println("Added " + added + " embeddings.");
        System.out.println();

        Random random = new Random(42);
        int numberOfQueries = 5;
        System.out.println("Running " + numberOfQueries + " random semantic queries:");
        System.out.println("=========================================");

        for (int i = 0; i < numberOfQueries; i++) {
            String queryText = lines.get(random.nextInt(lines.size()));
            System.out.println("\nQuery " + (i + 1) + ": " + queryText);

            Embedding queryEmbedding = embeddingModel.embed(queryText).content();
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(3)
                    .build();

            EmbeddingSearchResult<TextSegment> result = store.search(request);
            System.out.println("Top 3 matches:");
            for (int j = 0; j < result.matches().size(); j++) {
                EmbeddingMatch<TextSegment> match = result.matches().get(j);
                System.out.printf("  %d. Score: %.4f - %s%n",
                        j + 1, match.score(), match.embedded().text());
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("%n=== Finished with %s in %d ms ===%n%n", storeName, elapsed);
    }

    /**
     * Demonstrates adding embeddings with metadata and filtering search results by metadata.
     *
     * <p>Three topics are indexed (science, history, technology), each tagged with a "topic"
     * and "difficulty" metadata key. The example then shows:
     * <ul>
     *   <li>Filtering by a single metadata value (topic = "science")</li>
     *   <li>Filtering by multiple values using IsIn (topic in {history, technology})</li>
     *   <li>Combining filters with And (topic = "technology" AND difficulty = "advanced")</li>
     * </ul>
     */
    private static void runMetadataFilteringExample(ArcadeDBEmbeddingStore store,
                                                    EmbeddingModel embeddingModel) {

        System.out.println("=== Metadata Filtering Example (Embedded) ===");

        // Add science documents
        addWithMetadata(store, embeddingModel,
                "Photosynthesis converts sunlight into chemical energy in plants.",
                "science", "beginner");
        addWithMetadata(store, embeddingModel,
                "Quantum entanglement links particles regardless of their distance.",
                "science", "advanced");
        addWithMetadata(store, embeddingModel,
                "DNA stores hereditary information using sequences of nucleotides.",
                "science", "intermediate");

        // Add history documents
        addWithMetadata(store, embeddingModel,
                "The Roman Empire fell in 476 AD after centuries of decline.",
                "history", "beginner");
        addWithMetadata(store, embeddingModel,
                "The French Revolution began in 1789 and reshaped European politics.",
                "history", "intermediate");

        // Add technology documents
        addWithMetadata(store, embeddingModel,
                "Machine learning enables computers to learn from data without explicit programming.",
                "technology", "beginner");
        addWithMetadata(store, embeddingModel,
                "Transformer neural networks revolutionised natural language processing.",
                "technology", "advanced");

        System.out.println("Added 7 embeddings with topic and difficulty metadata.");
        System.out.println();

        String queryText = "How do computers learn from examples?";
        Embedding queryEmbedding = embeddingModel.embed(queryText).content();
        System.out.println("Query: \"" + queryText + "\"");
        System.out.println();

        // Filter 1: only science documents
        Filter scienceFilter = new IsEqualTo("topic", "science");
        search(store, queryEmbedding, scienceFilter, "topic = 'science'");

        // Filter 2: history or technology documents
        Filter historyOrTechFilter = new IsIn("topic", List.of("history", "technology"));
        search(store, queryEmbedding, historyOrTechFilter, "topic in ['history', 'technology']");

        // Filter 3: advanced technology documents only
        Filter advancedTechFilter = new And(
                new IsEqualTo("topic", "technology"),
                new IsEqualTo("difficulty", "advanced")
        );
        search(store, queryEmbedding, advancedTechFilter, "topic = 'technology' AND difficulty = 'advanced'");

        System.out.println("=== Finished Metadata Filtering Example ===");
        System.out.println();
    }

    /**
     * Demonstrates that an embedded database is persistent: data written in one session is
     * still available when the database is reopened using the same {@code databasePath}.
     */
    private static void runPersistenceExample(Path dbPath, EmbeddingModel embeddingModel) {
        System.out.println("=== Persistence Example ===");
        System.out.println("Opening database for the first time and adding two embeddings...");

        // First session: write data
        ArcadeDBEmbeddingStore firstSession = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath.toString())
                .dimension(384)
                .build();

        String text1 = "The speed of light is approximately 299,792 km/s.";
        String text2 = "Newton's laws describe the motion of objects under force.";
        firstSession.add(embeddingModel.embed(TextSegment.from(text1)).content(), TextSegment.from(text1));
        firstSession.add(embeddingModel.embed(TextSegment.from(text2)).content(), TextSegment.from(text2));
        firstSession.close();
        System.out.println("First session closed.");

        // Second session: read the same data back
        System.out.println("Reopening the same database directory...");
        ArcadeDBEmbeddingStore secondSession = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath.toString())
                .dimension(384)
                .build();

        try {
            Embedding query = embeddingModel.embed("How fast does light travel?").content();
            EmbeddingSearchResult<TextSegment> result = secondSession.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(query)
                            .maxResults(2)
                            .build());

            System.out.println("Query: \"How fast does light travel?\"");
            System.out.println("Results from reopened database:");
            for (int i = 0; i < result.matches().size(); i++) {
                EmbeddingMatch<TextSegment> match = result.matches().get(i);
                System.out.printf("  %d. Score: %.4f - %s%n",
                        i + 1, match.score(), match.embedded().text());
            }
        } finally {
            secondSession.close();
        }

        System.out.println("=== Finished Persistence Example ===");
        System.out.println();
    }

    private static void addWithMetadata(EmbeddingStore<TextSegment> store,
                                        EmbeddingModel embeddingModel,
                                        String text,
                                        String topic,
                                        String difficulty) {
        Metadata metadata = Metadata.from("topic", topic);
        metadata.put("difficulty", difficulty);
        TextSegment segment = TextSegment.from(text, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();
        store.add(embedding, segment);
    }

    private static void search(ArcadeDBEmbeddingStore store,
                                Embedding queryEmbedding,
                                Filter filter,
                                String filterDescription) {
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .filter(filter)
                .build();

        EmbeddingSearchResult<TextSegment> result = store.search(request);
        System.out.println("Filter [" + filterDescription + "] — " + result.matches().size() + " match(es):");
        for (int i = 0; i < result.matches().size(); i++) {
            EmbeddingMatch<TextSegment> match = result.matches().get(i);
            System.out.printf("  %d. Score: %.4f | topic=%-12s | difficulty=%-12s | %s%n",
                    i + 1,
                    match.score(),
                    match.embedded().metadata().getString("topic"),
                    match.embedded().metadata().getString("difficulty"),
                    match.embedded().text());
        }
        System.out.println();
    }

    /**
     * Reads all non-empty lines from a classpath resource file.
     */
    private static List<String> readLinesFromResource(String resourceName) {
        List<String> lines = new ArrayList<>();
        try (InputStream inputStream = ArcadeDBEmbeddingStoreEmbeddedExample.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        lines.add(line);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource: " + resourceName, e);
        }
        return lines;
    }
}
