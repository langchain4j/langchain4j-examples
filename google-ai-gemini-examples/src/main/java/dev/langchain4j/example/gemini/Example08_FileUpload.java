package dev.langchain4j.example.gemini;

import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GeminiFiles;
import dev.langchain4j.model.googleai.GeminiFiles.GeminiFile;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import java.nio.file.Path;

/**
 * Demonstrates uploading a file and using it in a chat request with Google AI Gemini.
 *
 * <p>This example shows how to:
 * <ul>
 *   <li>Upload a file using the {@link GeminiFiles} API</li>
 *   <li>Reference the uploaded file in a chat request via its URI</li>
 *   <li>Combine file content with text prompts for multimodal interaction</li>
 *   <li>Clean up by deleting the file after use</li>
 * </ul>
 *
 * <p>This pattern is useful for processing large files or reusing the same file
 * across multiple chat requests without re-uploading.
 *
 * <p>Requires the {@code GOOGLE_AI_GEMINI_API_KEY} environment variable to be set.
 */
public class Example08_FileUpload {

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("GOOGLE_AI_GEMINI_API_KEY");

        GeminiFiles geminiFiles = GeminiFiles.builder()
                .apiKey(apiKey)
                .build();

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash-lite")
                .logRequestsAndResponses(true)
                .build();

        // Create a sample text file to upload
        var file = Path.of(Example08_FileUpload.class.getClassLoader().getResource("q4-planning.pdf").toURI());

        GeminiFile uploadedFile = null;
        try {
            // Upload the file
            System.out.println("Uploading file...");
            uploadedFile = geminiFiles.uploadFile(file, "meeting-notes.txt");

            // Wait for processing
            while (uploadedFile.isProcessing()) {
                Thread.sleep(500);
                uploadedFile = geminiFiles.getMetadata(uploadedFile.name());
            }

            if (uploadedFile.isFailed()) {
                System.err.println("File processing failed!");
                return;
            }

            System.out.println("File uploaded successfully: " + uploadedFile.uri());

            // Use the uploaded file in a chat request
            UserMessage userMessage = UserMessage.from(
                    PdfFileContent.from(uploadedFile.uri()),
                    TextContent.from("Summarize this document and list the action items with their owners.")
            );

            System.out.println("\nAsking Gemini to analyze the uploaded file...\n");

            ChatResponse response = model.chat(userMessage);

            System.out.println("Response:");
            System.out.println(response.aiMessage().text());

        } finally {
            // Clean up: delete uploaded file and local temp file
            if (uploadedFile != null) {
                geminiFiles.deleteFile(uploadedFile.name());
                System.out.println("\nUploaded file deleted.");
            }
        }
    }
}