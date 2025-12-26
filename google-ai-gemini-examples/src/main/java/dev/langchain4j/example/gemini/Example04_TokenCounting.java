package dev.langchain4j.example.gemini;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.googleai.GoogleAiGeminiTokenCountEstimator;

import java.util.List;

/**
 * Demonstrates token counting with Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Create a {@link GoogleAiGeminiTokenCountEstimator}</li>
 *   <li>Estimate tokens in plain text</li>
 *   <li>Estimate tokens in single and multiple messages</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 *
 * <p>Learn <a href="https://ai.google.dev/gemini-api/docs/tokens">more</a></p>
 */
public class Example04_TokenCounting {

    public static void main(String[] args) {
        GoogleAiGeminiTokenCountEstimator tokenEstimator = GoogleAiGeminiTokenCountEstimator.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-2.5-flash-lite")
                .build();

        // Count tokens in plain text
        String text = "The quick brown fox jumps over the lazy dog.";
        int textTokens = tokenEstimator.estimateTokenCountInText(text);
        System.out.println("Plain Text token count: " + textTokens);

        // Count tokens in a single message
        UserMessage userMessage = UserMessage.from("What is the capital of France?");
        int messageTokens = tokenEstimator.estimateTokenCountInMessage(userMessage);
        System.out.println("\nSingle message token count: " + messageTokens);

        // Count tokens in multiple messages
        SystemMessage systemMessage = SystemMessage.from("You are a helpful assistant.");
        UserMessage question = UserMessage.from("Explain quantum computing in simple terms.");
        int conversationTokens = tokenEstimator.estimateTokenCountInMessages(List.of(systemMessage, question));
        System.out.println("\nConversation token count: " + conversationTokens);
    }
}