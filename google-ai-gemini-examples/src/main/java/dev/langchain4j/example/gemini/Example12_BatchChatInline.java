package dev.langchain4j.example.gemini;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.googleai.BatchRequestResponse;
import dev.langchain4j.model.googleai.BatchRequestResponse.BatchIncomplete;
import dev.langchain4j.model.googleai.BatchRequestResponse.BatchName;
import dev.langchain4j.model.googleai.BatchRequestResponse.BatchResponse;
import dev.langchain4j.model.googleai.BatchRequestResponse.BatchSuccess;
import dev.langchain4j.model.googleai.GoogleAiGeminiBatchChatModel;

import java.lang.reflect.Method;
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

    public static void main(String[] args) throws Exception {
        var batchModel = GoogleAiGeminiBatchChatModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-2.5-flash-lite")
                .logRequestsAndResponses(true)
                .build();

        var requests = List.of(
                ChatRequest.builder()
                        .modelName("gemini-2.5-flash-lite")
                        .messages(UserMessage.from("What is the capital of France?"))
                        .build(),
                ChatRequest.builder()
                        .modelName("gemini-2.5-flash-lite")
                        .messages(UserMessage.from("What is the capital of Japan?"))
                        .build(),
                ChatRequest.builder()
                        .modelName("gemini-2.5-flash-lite")
                        .messages(UserMessage.from("What is the capital of Brazil?"))
                        .build()
        );

        System.out.println("Submitting batch with " + requests.size() + " requests...");

        BatchResponse<?> response = batchModel.createBatchInline("capitals-batch", 0L, requests);
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
            System.out.println("Results:");

            var results = success.responses();
            for (int i = 0; i < results.size(); i++) {
                var chatResponse = (dev.langchain4j.model.chat.response.ChatResponse) results.get(i);
                System.out.println("  " + (i + 1) + ". " + chatResponse.aiMessage().text());
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

    private static GoogleAiGeminiBatchChatModel createBatchModel() throws Exception {
        // Use reflection to access the non-public builder() method
        Method builderMethod = GoogleAiGeminiBatchChatModel.class.getDeclaredMethod("builder");
        builderMethod.setAccessible(true);
        Object builder = builderMethod.invoke(null);

        Class<?> builderClass = builder.getClass();

        Method apiKeyMethod = builderClass.getMethod("apiKey", String.class);
        builder = apiKeyMethod.invoke(builder, System.getenv("GOOGLE_AI_GEMINI_API_KEY"));

        Method modelNameMethod = builderClass.getMethod("modelName", String.class);
        builder = modelNameMethod.invoke(builder, "gemini-2.5-flash-lite");

        Method logMethodName = builderClass.getMethod("logRequestsAndResponses", Boolean.class);
        builder = logMethodName.invoke(builder, true);

        Method buildMethod = builderClass.getMethod("build");
        return (GoogleAiGeminiBatchChatModel) buildMethod.invoke(builder);
    }
}