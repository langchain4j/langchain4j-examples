package dev.langchain4j.example.gemini;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

/**
 * Demonstrates multimodal chat capabilities with Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Send images along with text prompts</li>
 *   <li>Use image URLs in chat requests</li>
 *   <li>Combine multiple content types in a single message</li>
 * </ul>
 *
 * <p>Gemini supports various multimodal inputs including images, audio, video, and PDFs.
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 */
public class Example09_MultimodalChat {

    public static void main(String[] args) {
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GOOGLE_AI_GEMINI_API_KEY"))
                .modelName("gemini-2.5-flash-lite")
                .build();

        // Example using a public image URL
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Cat03.jpg/1200px-Cat03.jpg";

        UserMessage userMessage = UserMessage.from(
                ImageContent.from(imageUrl),
                TextContent.from("What do you see in this image? Describe it in detail.")
        );

        System.out.println("Sending image with question to Gemini...\n");

        ChatResponse response = model.chat(userMessage);

        System.out.println("Response:");
        System.out.println(response.aiMessage().text());
    }
}