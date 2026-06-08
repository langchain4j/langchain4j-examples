package dev.langchain4j.example.gemini;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.batch.BatchResponse;
import dev.langchain4j.model.batch.BatchState;
import dev.langchain4j.model.googleai.BatchRequestResponse.BatchFileRequest;
import dev.langchain4j.model.googleai.GeminiFiles;
import dev.langchain4j.model.googleai.GoogleAiGeminiBatchEmbeddingModel;
import dev.langchain4j.model.googleai.jsonl.JsonLinesWriters;
import dev.langchain4j.model.output.Response;

import java.nio.file.Files;
import java.util.List;

/**
 * Demonstrates file-based batch embedding processing with Google AI Gemini.
 *
 * <p>File-based batching is ideal for very large batches that exceed the 20MB inline limit.
 * The workflow is:
 * <ol>
 *   <li>Write batch requests to a local JSONL file</li>
 *   <li>Upload the file using the Gemini Files API</li>
 *   <li>Submit a batch job referencing the uploaded file</li>
 *   <li>Poll for completion and retrieve results</li>
 *   <li>Process results</li>
 * </ol>
 *
 * <p>Benefits:
 * <ul>
 *   <li>50% cost reduction compared to interactive requests</li>
 *   <li>24-hour turnaround SLO</li>
 *   <li>Support for batches larger than 20MB</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 */
public class Example15_BatchEmbedFromFile {

    public static void main(String[] args) throws Exception {
        var apiKey = System.getenv("GOOGLE_AI_GEMINI_API_KEY");

        GoogleAiGeminiBatchEmbeddingModel batchModel = GoogleAiGeminiBatchEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-embedding-001")
                .logRequestsAndResponses(true)
                .build();

        var geminiFiles = GeminiFiles.builder().apiKey(apiKey).build();

        // Each request is paired with a unique key so results can be correlated back to the input.
        var segments = List.of(
                new BatchFileRequest<>("ai-text", TextSegment.from("Artificial intelligence is transforming industries worldwide.")),
                new BatchFileRequest<>("ml-text", TextSegment.from("Machine learning enables computers to learn from data.")),
                new BatchFileRequest<>("nlp-text", TextSegment.from("Natural language processing helps machines understand human language.")),
                new BatchFileRequest<>("dl-text", TextSegment.from("Deep learning uses neural networks with multiple layers."))
        );

        // Step 1: Write requests to a local JSONL file
        var tempFile = Files.createTempFile("batch-embed-requests-", ".jsonl");
        System.out.println("Writing batch embedding requests to: " + tempFile);
        try (var writer = JsonLinesWriters.streaming(tempFile)) {
            batchModel.writeBatchToFile(writer, segments);
        }

        // Verify JSONL content
        System.out.println("JSONL content:");
        Files.readAllLines(tempFile).forEach(line -> System.out.println("  " + line));

        // Step 2: Upload the file using Gemini Files API
        System.out.println("\nUploading file to Gemini Files API...");
        var uploadedFile = geminiFiles.uploadFile(tempFile, "batch-embed-requests.jsonl");
        System.out.println("Uploaded file URI: " + uploadedFile.uri());
        System.out.println("File state: " + uploadedFile.state());

        // Wait for file to become ACTIVE
        while (uploadedFile.isProcessing()) {
            System.out.println("  Waiting for file to become active...");
            Thread.sleep(2000);
            uploadedFile = geminiFiles.getMetadata(uploadedFile.name());
        }

        if (!uploadedFile.isActive()) {
            System.err.println("File upload failed: " + uploadedFile.state());
            return;
        }

        System.out.println("File is now ACTIVE");

        // Step 3: Submit a batch embedding job from the uploaded file
        System.out.println("\nSubmitting batch embedding job from uploaded file...");
        BatchResponse<Response<Embedding>> response = batchModel.submit("file-based-embedding-batch", uploadedFile);
        var batchId = response.batchId();

        System.out.println("Batch created: " + batchId);
        System.out.println("Polling for completion...");

        // Step 4: Poll until the batch reaches a terminal state
        while (!response.state().isTerminal()) {
            Thread.sleep(5000);
            response = batchModel.retrieve(batchId);
            System.out.println("  State: " + response.state());
        }

        // Step 5: Process results
        if (response.state() == BatchState.SUCCEEDED) {
            System.out.println("\nBatch completed successfully!");
            System.out.println("Results:");

            var responses = response.responses();
            for (int i = 0; i < responses.size(); i++) {
                Embedding embedding = responses.get(i).content();
                System.out.println("\n  " + (i + 1) + ". " + segments.get(i).key());
                System.out.println("     Dimension: " + embedding.dimension());
                System.out.print("     Vector (first 5 values): [");
                for (int j = 0; j < 5 && j < embedding.vector().length; j++) {
                    System.out.printf("%.6f%s", embedding.vector()[j], j < 4 ? ", " : "");
                }
                System.out.println("]");
            }
        } else {
            System.err.println("Batch did not succeed. State: " + response.state() + ", errors: " + response.errors());
        }

        // Clean up
        System.out.println("\nCleaning up...");
        batchModel.deleteBatchJob(batchId);
        System.out.println("Batch job deleted.");

        geminiFiles.deleteFile(uploadedFile.name());
        System.out.println("Uploaded file deleted.");

        Files.deleteIfExists(tempFile);
        System.out.println("Local temp file deleted.");
    }
}
