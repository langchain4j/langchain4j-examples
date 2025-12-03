package dev.langchain4j.example.gemini;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Demonstrates streaming chat responses with Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Create a {@link GoogleAiGeminiStreamingChatModel}</li>
 *   <li>Receive tokens as they are generated in real-time</li>
 *   <li>Handle completion and error callbacks</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 */
public class Example02_StreamingChat {
     static class WaitingChatResponseHandler implements StreamingChatResponseHandler {
        // Future used to wait for a response
        private CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        @Override
        public void onPartialResponse(String partialResponse) {
            System.out.print(partialResponse);
        }

        @Override
        public void onCompleteResponse(ChatResponse completeResponse) {
            System.out.println("\n\n--- Complete ---");
            System.out.println("Finish reason: " + completeResponse.finishReason());
            System.out.println("Tokens used: " + completeResponse.tokenUsage().totalTokenCount());
            System.out.println("Completed message text: " + completeResponse.aiMessage().text());
            futureResponse.complete(completeResponse);
        }

        @Override
        public void onError(Throwable error) {
            System.err.println("Error: " + error.getMessage());
        }
    };

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        var model = GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-2.5-flash-lite")
                .build();

        // Set up the handler - what to do when messages are received.
        var handler = new WaitingChatResponseHandler();

        // Send the chat request. This will start calling onPartialResponse as tokens are returned from Gemini. And
        // later calls onCompleteResponse - with the entire message - once Gemini is finished.
        model.chat("Explain quantum computing in simple terms", handler);

        // Wait for the future to complete or 60 seconds, whatever comes first.
        handler.futureResponse.get(60, SECONDS);
    }
}