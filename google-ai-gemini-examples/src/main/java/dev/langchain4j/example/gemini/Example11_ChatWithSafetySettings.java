package dev.langchain4j.example.gemini;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GeminiHarmBlockThreshold;
import dev.langchain4j.model.googleai.GeminiHarmCategory;
import dev.langchain4j.model.googleai.GeminiSafetySetting;

import java.util.List;

/**
 * Demonstrates configuring safety settings with Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Configure safety thresholds for different harm categories</li>
 *   <li>Customize content filtering behavior</li>
 *   <li>Balance safety with response flexibility</li>
 * </ul>
 *
 * <p>Available harm categories:
 * <ul>
 *   <li>{@code HARM_CATEGORY_HARASSMENT}</li>
 *   <li>{@code HARM_CATEGORY_HATE_SPEECH}</li>
 *   <li>{@code HARM_CATEGORY_SEXUALLY_EXPLICIT}</li>
 *   <li>{@code HARM_CATEGORY_DANGEROUS_CONTENT}</li>
 * </ul>
 *
 * <p>Available thresholds (from most to least restrictive):
 * <ul>
 *   <li>{@code BLOCK_LOW_AND_ABOVE}</li>
 *   <li>{@code BLOCK_MEDIUM_AND_ABOVE}</li>
 *   <li>{@code BLOCK_ONLY_HIGH}</li>
 *   <li>{@code BLOCK_NONE}</li>
 * </ul>
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 *
 * <p>Learn <a href="https://ai.google.dev/gemini-api/docs/safety-settings">more</a></p>
 */
public class Example11_ChatWithSafetySettings {
    public static void main(String[] args) {
        List<GeminiSafetySetting> safetySettings = List.of(
                new GeminiSafetySetting(
                        GeminiHarmCategory.HARM_CATEGORY_HARASSMENT,
                        GeminiHarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE),
                new GeminiSafetySetting(
                        GeminiHarmCategory.HARM_CATEGORY_HATE_SPEECH,
                        GeminiHarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE),
                new GeminiSafetySetting(
                        GeminiHarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT,
                        GeminiHarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE),
                new GeminiSafetySetting(
                        GeminiHarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
                        GeminiHarmBlockThreshold.BLOCK_ONLY_HIGH)
        );

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-2.5-flash-lite")
                .safetySettings(safetySettings)
                .build();

        System.out.println("Safety settings configured:");
        for (GeminiSafetySetting setting : safetySettings) {
            System.out.println("  " + setting.getCategory() + ": " + setting.getThreshold());
        }

        String prompt = "Explain common safety practices when handling kitchen knives.";

        System.out.println("\nPrompt: " + prompt);
        System.out.println("\nResponse:");

        ChatResponse response = model.chat(UserMessage.from(prompt));
        System.out.println(response.aiMessage().text());
    }
}