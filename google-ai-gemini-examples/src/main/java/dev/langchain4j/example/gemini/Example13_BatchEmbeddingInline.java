package dev.langchain4j.example.gemini;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.batch.BatchResponse;
import dev.langchain4j.model.batch.BatchState;
import dev.langchain4j.model.googleai.GeminiBatchRequest;
import dev.langchain4j.model.googleai.GoogleAiGeminiBatchEmbeddingModel;
import dev.langchain4j.model.output.Response;

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
        GoogleAiGeminiBatchEmbeddingModel batchModel = GoogleAiGeminiBatchEmbeddingModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-embedding-001")
                .logRequestsAndResponses(true)
                .build();

        List<TextSegment> segments = List.of(
                TextSegment.from("Artificial intelligence is transforming industries worldwide."),
                TextSegment.from("Machine learning models require large datasets for training."),
                TextSegment.from("Natural language processing enables computers to understand text.")
        );

        System.out.println("Submitting batch with " + segments.size() + " text segments...");

        // Submit the batch. The returned response carries the batch id and its current state.
        BatchResponse<Response<Embedding>> response =
                batchModel.submit(GeminiBatchRequest.from(segments, "embeddings-batch", 1L));
        var batchId = response.batchId();

        System.out.println("Batch created: " + batchId);
        System.out.println("Polling for completion...");

        // Poll until the batch reaches a terminal state.
        while (!response.state().isTerminal()) {
            Thread.sleep(5000);
            response = batchModel.retrieve(batchId);
            System.out.println("  State: " + response.state());
        }

        // Process results.
        if (response.state() == BatchState.SUCCEEDED) {
            System.out.println("\nBatch completed successfully!");
            System.out.println("Embeddings:");

            var responses = response.responses();
            for (int i = 0; i < responses.size(); i++) {
                Embedding embedding = responses.get(i).content();
                System.out.printf("  %d. Dimension: %d, First 10 values: [", i + 1, embedding.dimension());
                float[] vector = embedding.vector();
                for (int j = 0; j < Math.min(10, vector.length); j++) {
                    System.out.printf("%.4f%s", vector[j], j < 9 ? ", " : "");
                }
                System.out.println("...]");
            }
        } else {
            System.err.println("Batch did not succeed. State: " + response.state() + ", errors: " + response.errors());
        }

        // Clean up.
        batchModel.deleteBatchJob(batchId);
        System.out.println("\nBatch job deleted.");
    }
}
