import dev.langchain4j.community.store.embedding.arcadedb.ArcadeDBEmbeddingStore;
import dev.langchain4j.community.store.embedding.arcadedb.ArcadeDBFilterStrategy;
import dev.langchain4j.community.store.embedding.arcadedb.ArcadeDBHybridFusion;
import dev.langchain4j.community.store.embedding.arcadedb.ArcadeDBVectorQuantization;
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
 * (or created, if it does not yet exist) automatically.  The store is {@link java.io.Closeable}:
 * close it when you are finished, for example with try-with-resources, to release the database.
 *
 * <p>For usage against a remote ArcadeDB server, see {@link ArcadeDBEmbeddingStoreRemoteExample}.
 *
 * <p>This example demonstrates:
 * <ul>
 *   <li>Basic embedded store usage, with batched ingestion and vector index options</li>
 *   <li>Hybrid search: vector search combined with full-text (BM25) search</li>
 *   <li>Metadata-enriched embeddings and metadata filtering, in vector and hybrid search</li>
 *   <li>Upserts: adding embeddings with existing ids replaces them</li>
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

        try (ArcadeDBEmbeddingStore basicStore = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath1.toString())
                .dimension(384)
                .similarityFunction("COSINE")                     // Default: COSINE; also EUCLIDEAN, DOT_PRODUCT
                .quantization(ArcadeDBVectorQuantization.INT8)    // Default: NONE; INT8 uses about 4x less index memory
                .maxConnections(16)                               // HNSW connections per node (index build)
                .beamWidth(100)                                   // HNSW beam width while building the index
                .efSearch(200)                                    // Search beam width per query: higher = better recall
                .batchSize(1000)                                  // Records per transaction in addAll
                .build()) {
            runBasicSearch(basicStore, embeddingModel, lines, "ArcadeDB Embedded (basic)");
        }

        // Example 2: hybrid search — vector search combined with full-text search
        Path dbPath2 = Files.createTempDirectory("arcadedb-example-hybrid");
        System.out.println("Database path: " + dbPath2);

        try (ArcadeDBEmbeddingStore hybridStore = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath2.toString())
                .dimension(384)
                // The defaults are shown explicitly here: RRF, weighted 8:1 towards the vector ranking,
                // and an English analyzer (stop words, stemming) for the full-text index.
                .hybridFusion(ArcadeDBHybridFusion.RRF)
                .hybridWeights(8, 1)
                .fullTextAnalyzer("org.apache.lucene.analysis.en.EnglishAnalyzer")
                .build()) {
            runHybridSearchExample(hybridStore, embeddingModel, lines);
        }

        // Example 3: embedded store with metadata filtering
        Path dbPath3 = Files.createTempDirectory("arcadedb-example-filtering");
        System.out.println("Database path: " + dbPath3);

        try (ArcadeDBEmbeddingStore filteringStore = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath3.toString())
                .dimension(384)
                // Default: ADAPTIVE — filters run in the database, so even a filter that matches few
                // records returns complete results; POST_FILTER restores filtering of a fixed candidate set.
                .filterStrategy(ArcadeDBFilterStrategy.ADAPTIVE)
                .build()) {
            runMetadataFilteringExample(filteringStore, embeddingModel);
        }

        // Example 4: upserts — adding embeddings with existing ids replaces them
        Path dbPath4 = Files.createTempDirectory("arcadedb-example-upsert");
        System.out.println("Database path: " + dbPath4);

        try (ArcadeDBEmbeddingStore upsertStore = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath4.toString())
                .dimension(384)
                .build()) {
            runUpsertExample(upsertStore, embeddingModel);
        }

        // Example 5: reopen a persistent database — same databasePath, data is still there
        Path dbPath5 = Files.createTempDirectory("arcadedb-example-persistent");
        System.out.println("Database path: " + dbPath5);

        runPersistenceExample(dbPath5, embeddingModel);
    }

    /**
     * Adds all lines from the test document to the store in one batched call and runs random semantic queries.
     */
    private static void runBasicSearch(EmbeddingStore<TextSegment> store,
                                       EmbeddingModel embeddingModel,
                                       List<String> lines,
                                       String storeName) {

        long startTime = System.currentTimeMillis();
        System.out.println("=== Running with store: " + storeName + " ===");
        System.out.println("Adding embeddings...");

        addLines(store, embeddingModel, lines);
        System.out.println("Added " + lines.size() + " embeddings.");
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
            printMatches(result);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("%n=== Finished with %s in %d ms ===%n%n", storeName, elapsed);
    }

    /**
     * Demonstrates hybrid search: a search request with query text runs a vector search and a full-text (BM25)
     * search, and fuses their rankings. Queries with names, numbers or rare words are where the full-text side
     * helps most. Without query text the same store runs a vector search only.
     */
    private static void runHybridSearchExample(ArcadeDBEmbeddingStore store,
                                               EmbeddingModel embeddingModel,
                                               List<String> lines) {

        System.out.println("=== Hybrid Search Example (Embedded) ===");
        addLines(store, embeddingModel, lines);
        System.out.println("Added " + lines.size() + " embeddings.");
        System.out.println();

        List<String> questions = List.of(
                "What did Naismith invent?",
                "What was finished in 1889?",
                "Which plays did Shakespeare write?");

        for (String question : questions) {
            Embedding questionEmbedding = embeddingModel.embed(question).content();
            System.out.println("Question: \"" + question + "\"");

            EmbeddingSearchResult<TextSegment> vectorOnly = store.search(EmbeddingSearchRequest.builder()
                    .queryEmbedding(questionEmbedding)
                    .maxResults(3)
                    .build());
            System.out.println("Vector search:");
            printMatches(vectorOnly);

            EmbeddingSearchResult<TextSegment> hybrid = store.search(EmbeddingSearchRequest.builder()
                    .queryEmbedding(questionEmbedding) // For the vector search
                    .query(question)                   // For the full-text search
                    .maxResults(3)
                    .build());
            System.out.println("Hybrid search (fused order; score = vector similarity):");
            printMatches(hybrid);
            System.out.println();
        }

        // Query text is escaped, so Lucene operators and special characters cannot break the query.
        // Build the store with rawFullTextQuery(true) to pass Lucene syntax such as "exact phrase" through.
        String withSpecialCharacters = "speed of light (in vacuum)?";
        EmbeddingSearchResult<TextSegment> escaped = store.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(withSpecialCharacters).content())
                .query(withSpecialCharacters)
                .maxResults(2)
                .build());
        System.out.println("Question with special characters: \"" + withSpecialCharacters + "\"");
        printMatches(escaped);

        System.out.println("=== Finished Hybrid Search Example ===");
        System.out.println();
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
     *   <li>Filtering a hybrid search</li>
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
        search(store, queryEmbedding, null, scienceFilter, "topic = 'science'");

        // Filter 2: history or technology documents
        Filter historyOrTechFilter = new IsIn("topic", List.of("history", "technology"));
        search(store, queryEmbedding, null, historyOrTechFilter, "topic in ['history', 'technology']");

        // Filter 3: advanced technology documents only
        Filter advancedTechFilter = new And(
                new IsEqualTo("topic", "technology"),
                new IsEqualTo("difficulty", "advanced")
        );
        search(store, queryEmbedding, null, advancedTechFilter, "topic = 'technology' AND difficulty = 'advanced'");

        // Filter 4: a hybrid search with a filter — both the vector and the full-text results are filtered
        String hybridQuery = "When did the Roman Empire fall?";
        System.out.println("Hybrid query: \"" + hybridQuery + "\"");
        search(store, embeddingModel.embed(hybridQuery).content(), hybridQuery, new IsEqualTo("topic", "history"),
                "topic = 'history' (hybrid)");

        System.out.println("=== Finished Metadata Filtering Example ===");
        System.out.println();
    }

    /**
     * Demonstrates upserts: {@code addAll} with ids that already exist replaces those records instead of failing,
     * so re-ingesting a changed document with stable ids keeps one record per id.
     */
    private static void runUpsertExample(ArcadeDBEmbeddingStore store, EmbeddingModel embeddingModel) {
        System.out.println("=== Upsert Example (Embedded) ===");

        List<String> ids = List.of("tower-height", "tower-city");
        List<TextSegment> original = List.of(
                TextSegment.from("The Eiffel Tower is about 300 meters tall."),
                TextSegment.from("The Eiffel Tower stands in Paris."));
        store.addAll(ids, embeddingModel.embedAll(original).content(), original);
        System.out.println("Added 2 embeddings with ids " + ids + ".");

        // Same ids, corrected text: the stored records are replaced
        List<TextSegment> corrected = List.of(
                TextSegment.from("The Eiffel Tower is about 330 meters tall, including its antennas."),
                TextSegment.from("The Eiffel Tower stands in Paris, on the Champ de Mars."));
        store.addAll(ids, embeddingModel.embedAll(corrected).content(), corrected);
        System.out.println("Added the same ids again with corrected text.");

        String queryText = "How tall is the Eiffel Tower?";
        EmbeddingSearchResult<TextSegment> result = store.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(queryText).content())
                .maxResults(10)
                .build());
        System.out.println("Query: \"" + queryText + "\" — " + result.matches().size() + " record(s) in the store:");
        for (EmbeddingMatch<TextSegment> match : result.matches()) {
            System.out.printf("  id=%-13s | Score: %.4f - %s%n", match.embeddingId(), match.score(), match.embedded().text());
        }

        System.out.println("=== Finished Upsert Example ===");
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
        try (ArcadeDBEmbeddingStore firstSession = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath.toString())
                .dimension(384)
                .build()) {
            String text1 = "The speed of light is approximately 299,792 km/s.";
            String text2 = "Newton's laws describe the motion of objects under force.";
            firstSession.add(embeddingModel.embed(TextSegment.from(text1)).content(), TextSegment.from(text1));
            firstSession.add(embeddingModel.embed(TextSegment.from(text2)).content(), TextSegment.from(text2));
        }
        System.out.println("First session closed.");

        // Second session: read the same data back
        System.out.println("Reopening the same database directory...");
        try (ArcadeDBEmbeddingStore secondSession = ArcadeDBEmbeddingStore.embeddedBuilder()
                .databasePath(dbPath.toString())
                .dimension(384)
                .build()) {
            Embedding query = embeddingModel.embed("How fast does light travel?").content();
            EmbeddingSearchResult<TextSegment> result = secondSession.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(query)
                            .maxResults(2)
                            .build());

            System.out.println("Query: \"How fast does light travel?\"");
            System.out.println("Results from reopened database:");
            printMatches(result);
        }

        System.out.println("=== Finished Persistence Example ===");
        System.out.println();
    }

    /**
     * Embeds all lines and adds them in one {@code addAll} call; the store writes them in batches of {@code batchSize}.
     */
    private static void addLines(EmbeddingStore<TextSegment> store, EmbeddingModel embeddingModel, List<String> lines) {
        List<TextSegment> segments = lines.stream().map(TextSegment::from).toList();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        store.addAll(embeddings, segments);
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

    /**
     * Runs a filtered search: a vector search, or a hybrid search when {@code queryText} is not null.
     */
    private static void search(ArcadeDBEmbeddingStore store,
                               Embedding queryEmbedding,
                               String queryText,
                               Filter filter,
                               String filterDescription) {
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .query(queryText)
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

    private static void printMatches(EmbeddingSearchResult<TextSegment> result) {
        for (int i = 0; i < result.matches().size(); i++) {
            EmbeddingMatch<TextSegment> match = result.matches().get(i);
            System.out.printf("  %d. Score: %.4f - %s%n", i + 1, match.score(), match.embedded().text());
        }
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
