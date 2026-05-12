package dev.langchain4j.example.gemini;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

/**
 * A simple example demonstrating basic chat functionality with Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Create a {@link GoogleAiGeminiChatModel} using the builder pattern</li>
 *   <li>Send a simple text message and receive a response</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set or by other means to get the API key.
 */
public class Example01_SimpleChat {
    public static void main(String[] args) {
        String apiKey = System.getenv("GOOGLE_AI_GEMINI_API_KEY");
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash-lite")
                .build();

        String response = model.chat("What is the capital of France?");

        System.out.println(response);
    }
}