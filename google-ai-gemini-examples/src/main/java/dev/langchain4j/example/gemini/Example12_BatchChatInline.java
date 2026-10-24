package dev.langchain4j.example.gemini;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.batch.BatchResponse;
import dev.langchain4j.model.batch.BatchState;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GeminiBatchRequest;
import dev.langchain4j.model.googleai.GoogleAiGeminiBatchChatModel;

import java.util.List;

/**
 * Demonstrates inline batch chat processing with Google AI Gemini.
 *
 * <p>Batch processing is ideal for large-scale, non-urgent tasks, offering:
 * <ul>
 *   <li>50% cost reduction compared to interactive requests</li>
 *   <li>24-hour turnaround SLO</li>
 *   <li>Up to 20MB of inline requests per batch</li>
 * </ul>
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Create a batch chat model</li>
 *   <li>Submit multiple chat requests as an inline batch</li>
 *   <li>Poll for batch completion</li>
 *   <li>Retrieve and process batch results</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 */
public class Example12_BatchChatInline {

    private static final String MODEL_NAME = "gemini-2.5-flash-lite";

    public static void main(String[] args) throws Exception {
        var batchModel = GoogleAiGeminiBatchChatModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName(MODEL_NAME)
                .logRequestsAndResponses(true)
                .build();

        var requests = List.of(
                ChatRequest.builder()
                        .modelName(MODEL_NAME)
                        .messages(UserMessage.from("What is the capital of France?"))
                        .build(),
                ChatRequest.builder()
                        .modelName(MODEL_NAME)
                        .messages(UserMessage.from("What is the capital of Japan?"))
                        .build(),
                ChatRequest.builder()
                        .modelName(MODEL_NAME)
                        .messages(UserMessage.from("What is the capital of Brazil?"))
                        .build()
        );

        System.out.println("Submitting batch with " + requests.size() + " requests...");

        // Submit the batch. The returned response carries the batch id and its current state.
        BatchResponse<ChatResponse> response =
                batchModel.submit(GeminiBatchRequest.from(requests, "capitals-batch", 0L));
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
            System.out.println("Results:");

            var responses = response.responses();
            for (int i = 0; i < responses.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + responses.get(i).aiMessage().text());
            }
        } else {
            System.err.println("Batch did not succeed. State: " + response.state() + ", errors: " + response.errors());
        }

        // Clean up.
        batchModel.deleteBatchJob(batchId);
        System.out.println("\nBatch job deleted.");
    }
}
