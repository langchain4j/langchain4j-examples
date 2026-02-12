package dev.langchain4j.example.gemini;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.googleai.BatchRequestResponse.BatchIncomplete;
import dev.langchain4j.model.googleai.BatchRequestResponse.BatchName;
import dev.langchain4j.model.googleai.BatchRequestResponse.BatchResponse;
import dev.langchain4j.model.googleai.BatchRequestResponse.BatchSuccess;
import dev.langchain4j.model.googleai.GoogleAiGeminiBatchEmbeddingModel;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Demonstrates inline batch embedding processing with Google AI Gemini.
 *
 * <p>Batch processing is ideal for large-scale, non-urgent embedding tasks, offering:
 * <ul>
 *   <li>50% cost reduction compared to interactive requests</li>
 *   <li>24-hour turnaround SLO</li>
 *   <li>Up to 20MB of inline requests per batch</li>
 *   <li>Up to 100 segments per batch request</li>
 * </ul>
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Create a batch embedding model</li>
 *   <li>Submit multiple text segments as an inline batch</li>
 *   <li>Poll for batch completion</li>
 *   <li>Retrieve and process embedding results</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 */
public class Example13_BatchEmbeddingInline {

    public static void main(String[] args) throws Exception {
        GoogleAiGeminiBatchEmbeddingModel batchModel =
                GoogleAiGeminiBatchEmbeddingModel.builder().apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                        .modelName("gemini-embedding-001")
                        .logRequestsAndResponses(true)
                        .build();

        List<TextSegment> segments = List.of(
                TextSegment.from("Artificial intelligence is transforming industries worldwide."),
                TextSegment.from("Machine learning models require large datasets for training."),
                TextSegment.from("Natural language processing enables computers to understand text.")
        );

        System.out.println("Submitting batch with " + segments.size() + " text segments...");

        BatchResponse<?> response = batchModel.createBatchInline("embeddings-batch", 1L, segments);
        BatchName batchName = getBatchName(response);

        System.out.println("Batch created: " + batchName.value());
        System.out.println("Polling for completion...");

        // Poll until complete
        do {
            Thread.sleep(5000);
            response = batchModel.retrieveBatchResults(batchName);
            System.out.println("  Status: " + response.getClass().getSimpleName());
        } while (response instanceof BatchIncomplete);

        // Process results
        if (response instanceof BatchSuccess<?> success) {
            System.out.println("\nBatch completed successfully!");
            System.out.println("Embeddings:");

            var results = success.responses();
            for (int i = 0; i < results.size(); i++) {
                var embedding = (Embedding) results.get(i);
                System.out.printf("  %d. Dimension: %d, First 10 values: [", i + 1, embedding.dimension());
                float[] vector = embedding.vector();
                for (int j = 0; j < Math.min(10, vector.length); j++) {
                    System.out.printf("%.4f%s", vector[j], j < 9 ? ", " : "");
                }
                System.out.println("...]");
            }
        } else {
            System.err.println("Batch failed: " + response);
        }

        // Clean up
        batchModel.deleteBatchJob(batchName);
        System.out.println("\nBatch job deleted.");
    }

    private static BatchName getBatchName(BatchResponse<?> response) {
        if (response instanceof BatchSuccess<?> success) {
            return success.batchName();
        } else if (response instanceof BatchIncomplete incomplete) {
            return incomplete.batchName();
        } else {
            throw new IllegalStateException("Unexpected response type: " + response);
        }
    }
}