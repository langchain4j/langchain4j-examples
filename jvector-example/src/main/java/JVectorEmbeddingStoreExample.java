import dev.langchain4j.community.store.embedding.jvector.JVectorEmbeddingStore;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JVectorEmbeddingStoreExample {

    private final static String TEST_DOCUMENT = "test-document.txt";
    public static final String TMP_JVECTOR_EMBEDDING_STORE = "/tmp/jvector-embedding-store";

    public static void main(String[] args) {

        File tempPath = new File(TMP_JVECTOR_EMBEDDING_STORE);
        try {
            // Create the default embedding model
            EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

            // Read all lines from the text file once
            if (JVectorEmbeddingStoreExample.class.getClassLoader().getResource(TEST_DOCUMENT) == null) {
                throw new RuntimeException("Test document not found: " + TEST_DOCUMENT);
            }
            List<String> lines = readLinesFromResource(TEST_DOCUMENT);
            System.out.println("Read " + lines.size() + " lines from " + TEST_DOCUMENT);
            System.out.println();

            if (!tempPath.exists()) {
                boolean created = tempPath.mkdirs();
                if (!created) {
                    throw new RuntimeException("Failed to create temporary directory: " + TMP_JVECTOR_EMBEDDING_STORE);
                }
            }
            File indexPath = new File(tempPath, "example-index");

            // First store: JVector with maxDegree 16
            EmbeddingStore<TextSegment> jvectorStore1 = JVectorEmbeddingStore.builder()
                    .dimension(384)
                    .maxDegree(16)
                    .build();

            runWithStore(jvectorStore1, embeddingModel, lines, "JVectorStore(maxDegree=16)");

            // Second store: JVector with a different configuration (maxDegree 8)
            EmbeddingStore<TextSegment> jvectorStore2 = JVectorEmbeddingStore.builder()
                    .dimension(384)
                    .maxDegree(8)
                    .persistencePath(indexPath.getAbsolutePath())
                    .build();

            runWithStore(jvectorStore2, embeddingModel, lines, "JVectorStore(maxDegree=8)");
        } catch (Exception e) {
            System.err.println("Failed to run the example due to: " + e.getMessage());
        } finally {
            // Clean up the temporary index directory
            if (tempPath.exists() && tempPath.isDirectory()) {
                final File[] files = tempPath.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.delete()) {
                            System.err.println("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * Runs the common workflow against the provided embedding store.
     */
    private static void runWithStore(EmbeddingStore<TextSegment> embeddingStore,
                                     EmbeddingModel embeddingModel,
                                     List<String> lines,
                                     String storeName) {

        long startTime = System.currentTimeMillis();
        System.out.println("=== Running with store: " + storeName + " ===");
        System.out.println("Adding embeddings to the store...");
        int added = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                TextSegment segment = TextSegment.from(line);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
                added++;
            }
        }
        System.out.println("Successfully added " + added + " embeddings to the store");
        System.out.println();

        // Query the store with random lines from the file
        Random random = new Random();
        int numberOfQueries = 5;

        System.out.println("Querying the embedding store with " + numberOfQueries + " random lines:");
        System.out.println("=========================================");

        for (int i = 0; i < numberOfQueries; i++) {
            String randomLine = lines.get(random.nextInt(lines.size()));
            System.out.println("\nQuery " + (i + 1) + ": " + randomLine);

            Embedding queryEmbedding = embeddingModel.embed(randomLine).content();

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(3)
                    .build();

            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);
            List<EmbeddingMatch<TextSegment>> matches = result.matches();

            System.out.println("Top 3 matches:");
            for (int j = 0; j < matches.size(); j++) {
                EmbeddingMatch<TextSegment> match = matches.get(j);
                System.out.printf("  %d. Score: %.4f - %s%n",
                        j + 1,
                        match.score(),
                        match.embedded().text());
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("=== Finished running with store: %s in %d msec. ===%n",
                storeName, (endTime - startTime));
        System.out.println();
    }

    /**
     * Read all lines from a resource file
     */
    private static List<String> readLinesFromResource(String resourceName) {
        List<String> lines = new ArrayList<>();
        try (InputStream inputStream = JVectorEmbeddingStoreExample.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource file: " + resourceName, e);
        }
        return lines;
    }
}
