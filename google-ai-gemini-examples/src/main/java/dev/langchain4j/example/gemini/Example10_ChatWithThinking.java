package dev.langchain4j.example.gemini;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GeminiThinkingConfig;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

/**
 * Demonstrates the thinking/reasoning capability of Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Enable thinking mode using {@link GeminiThinkingConfig}</li>
 *   <li>Configure the thinking budget (token limit for reasoning)</li>
 *   <li>Access the model's reasoning process alongside the final response</li>
 * </ul>
 *
 * <p>Thinking mode allows the model to perform step-by-step reasoning before
 * providing a final answer, which is especially useful for complex problems.
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 */
public class Example10_ChatWithThinking {

    public static void main(String[] args) {
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-2.5-flash-lite")
                .thinkingConfig(GeminiThinkingConfig.builder()
                        .thinkingBudget(2048)
                        .build())
                .returnThinking(true)
                .build();

        String problem = """
                You are in a room with three light switches, each controlling one of three light bulbs in another room
                you cannot see from where you are. You can flip the switches as many times as you want, but you can
                only enter the room with the bulbs once. How do you determine which switch controls which bulb?
                """;

        System.out.println("Problem: " + problem);
        System.out.println("Asking Gemini with thinking enabled...\n");

        ChatResponse response = model.chat(UserMessage.from(problem));

        // Display thinking process if available
        if (response.aiMessage().thinking() != null) {
            System.out.println("=== Thinking Process ===");
            System.out.println(response.aiMessage().thinking());
            System.out.println();
        }

        System.out.println("=== Final Answer ===");
        System.out.println(response.aiMessage().text());
    }
}