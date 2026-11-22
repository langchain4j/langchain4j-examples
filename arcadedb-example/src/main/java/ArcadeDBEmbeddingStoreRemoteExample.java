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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Example demonstrating the ArcadeDB embedding store in <b>remote</b> mode, where the client
 * connects to an ArcadeDB server over HTTP.
 *
 * <p>Requires a running ArcadeDB instance. The quickest way to start one is via Docker:
 * <pre>
 *   docker run -d \
 *     --name arcadedb \
 *     -p 2480:2480 \
 *     -e JAVA_OPTS="-Darcadedb.server.rootPassword=playwithdata" \
 *     arcadedata/arcadedb:26.10.2
 * </pre>
 *
 * <p>For an alternative that requires no server setup, see
 * {@link ArcadeDBEmbeddingStoreEmbeddedExample}, which runs ArcadeDB embedded in the same JVM.
 *
 * <p>The databases created by this example persist on the server. Every embedding is added with a
 * stable id, so running the example again replaces the records (upsert) instead of adding duplicates.
 *
 * <p>This example demonstrates:
 * <ul>
 *   <li>Basic embedding store usage with COSINE similarity and batched ingestion</li>
 *   <li>Hybrid search: vector search combined with full-text (BM25) search</li>
 *   <li>Metadata-enriched embeddings and metadata filtering, in vector and hybrid search</li>
 *   <li>Upserts: adding embeddings with existing ids replaces them</li>
 *   <li>Using a different similarity function (EUCLIDEAN) and a quantized vector index</li>
 * </ul>
 */
public class ArcadeDBEmbeddingStoreRemoteExample {

    // ArcadeDB connection settings — adjust these to match your environment
    private static final String HOST = "localhost";
    private static final int PORT = 2480;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "playwithdata";

    private static final String TEST_DOCUMENT = "test-document.txt";

    public static void main(String[] args) {
        try {
            EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

            List<String> lines = readLinesFromResource(TEST_DOCUMENT);
            System.out.println("Read " + lines.size() + " lines from " + TEST_DOCUMENT);
            System.out.println();

            // Example 1: basic store with COSINE similarity (default)
            EmbeddingStore<TextSegment> cosineStore = ArcadeDBEmbeddingStore.builder()
                    .host(HOST)
                    .port(PORT)
                    .databaseName("example_cosine")
                    .username(USERNAME)
                    .password(PASSWORD)
                    .typeName("EmbeddingDocument")
                    .dimension(384)
                    .similarityFunction("COSINE")
                    .maxConnections(16)
                    .beamWidth(100)
                    .batchSize(500)            // Records per request and transaction in addAll
                    .createDatabase(true)
                    .build();

            runBasicSearch(cosineStore, embeddingModel, lines, "ArcadeDB(COSINE)");

            // Example 2: hybrid search — vector search combined with full-text search
            ArcadeDBEmbeddingStore hybridStore = ArcadeDBEmbeddingStore.builder()
                    .host(HOST)
                    .port(PORT)
                    .databaseName("example_hybrid")
                    .username(USERNAME)
                    .password(PASSWORD)
                    .dimension(384)
                    // The defaults are shown explicitly here: RRF, weighted 8:1 towards the vector ranking,
                    // and an English analyzer (stop words, stemming) for the full-text index.
                    .hybridFusion(ArcadeDBHybridFusion.RRF)
                    .hybridWeights(8, 1)
                    .fullTextAnalyzer("org.apache.lucene.analysis.en.EnglishAnalyzer")
                    .createDatabase(true)
                    .build();

            runHybridSearchExample(hybridStore, embeddingModel, lines);

            // Example 3: store with metadata filtering
            ArcadeDBEmbeddingStore filteringStore = ArcadeDBEmbeddingStore.builder()
                    .host(HOST)
                    .port(PORT)
                    .databaseName("example_filtering")
                    .username(USERNAME)
                    .password(PASSWORD)
                    .typeName("EmbeddingDocument")
                    .dimension(384)
                    .similarityFunction("COSINE")
                    // Default: ADAPTIVE — in remote mode, filters run on the server, so even a filter that
                    // matches few records returns complete results.
                    .filterStrategy(ArcadeDBFilterStrategy.ADAPTIVE)
                    .createDatabase(true)
                    .build();

            runMetadataFilteringExample(filteringStore, embeddingModel);

            // Example 4: upserts — adding embeddings with existing ids replaces them
            ArcadeDBEmbeddingStore upsertStore = ArcadeDBEmbeddingStore.builder()
                    .host(HOST)
                    .port(PORT)
                    .databaseName("example_upsert")
                    .username(USERNAME)
                    .password(PASSWORD)
                    .dimension(384)
                    .createDatabase(true)
                    .build();

            runUpsertExample(upsertStore, embeddingModel);

            // Example 5: store with EUCLIDEAN similarity and an INT8-quantized vector index
            EmbeddingStore<TextSegment> euclideanStore = ArcadeDBEmbeddingStore.builder()
                    .host(HOST)
                    .port(PORT)
                    .databaseName("example_euclidean")
                    .username(USERNAME)
                    .password(PASSWORD)
                    .typeName("EmbeddingDocument")
                    .dimension(384)
                    .similarityFunction("EUCLIDEAN")                  // Score: 1 / (1 + distance)
                    .quantization(ArcadeDBVectorQuantization.INT8)    // About 4x less index memory
                    .maxConnections(32)
                    .beamWidth(200)
                    .efSearch(200)                                    // Search beam width per query
                    .createDatabase(true)
                    .build();

            runBasicSearch(euclideanStore, embeddingModel, lines,
                    "ArcadeDB(EUCLIDEAN, INT8, maxConnections=32, beamWidth=200, efSearch=200)");

        } catch (Exception e) {
            System.err.println("Failed to run the example: " + e.getMessage());
            e.printStackTrace();
        }
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

        System.out.println("=== Hybrid Search Example ===");
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

        System.out.println("=== Metadata Filtering Example ===");

        // Add science documents
        addWithMetadata(store, embeddingModel, "science-1",
                "Photosynthesis converts sunlight into chemical energy in plants.",
                "science", "beginner");
        addWithMetadata(store, embeddingModel, "science-2",
                "Quantum entanglement links particles regardless of their distance.",
                "science", "advanced");
        addWithMetadata(store, embeddingModel, "science-3",
                "DNA stores hereditary information using sequences of nucleotides.",
                "science", "intermediate");

        // Add history documents
        addWithMetadata(store, embeddingModel, "history-1",
                "The Roman Empire fell in 476 AD after centuries of decline.",
                "history", "beginner");
        addWithMetadata(store, embeddingModel, "history-2",
                "The French Revolution began in 1789 and reshaped European politics.",
                "history", "intermediate");

        // Add technology documents
        addWithMetadata(store, embeddingModel, "technology-1",
                "Machine learning enables computers to learn from data without explicit programming.",
                "technology", "beginner");
        addWithMetadata(store, embeddingModel, "technology-2",
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
        System.out.println("=== Upsert Example ===");

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
     * Embeds all lines and adds them in one {@code addAll} call, with the ids {@code line-1}, {@code line-2}, …
     * The store sends them in batches of {@code batchSize}; on a second run the same ids replace the records.
     */
    private static void addLines(EmbeddingStore<TextSegment> store, EmbeddingModel embeddingModel, List<String> lines) {
        List<String> ids = IntStream.rangeClosed(1, lines.size()).mapToObj(i -> "line-" + i).toList();
        List<TextSegment> segments = lines.stream().map(TextSegment::from).toList();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        store.addAll(ids, embeddings, segments);
    }

    private static void addWithMetadata(EmbeddingStore<TextSegment> store,
                                        EmbeddingModel embeddingModel,
                                        String id,
                                        String text,
                                        String topic,
                                        String difficulty) {
        Metadata metadata = Metadata.from("topic", topic);
        metadata.put("difficulty", difficulty);
        TextSegment segment = TextSegment.from(text, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();
        store.addAll(List.of(id), List.of(embedding), List.of(segment));
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
        try (InputStream inputStream = ArcadeDBEmbeddingStoreRemoteExample.class
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
