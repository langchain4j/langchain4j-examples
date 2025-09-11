package util;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

/**
 * Utility class for providing configured ChatModel instances.
 * Centralizes the ChatModel configuration used across all examples.
 */
public class ChatModelProvider {
    
    /**
     * Creates a configured ChatModel instance with logging enabled.
     * Uses GPT-4O Mini model with request/response logging.
     * 
     * @return a configured ChatModel instance
     */
    public static ChatModel createChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
    
    /**
     * Creates a configured ChatModel instance with optional logging.
     * 
     * @param enableLogging whether to enable request/response logging
     * @return a configured ChatModel instance
     */
    public static ChatModel createChatModel(boolean enableLogging) {
        return createChatModel("OPENAI", enableLogging);
    }
    
    /**
     * Creates a configured ChatModel instance for the specified provider.
     * 
     * @param provider the model provider ("OPENAI" or "CEREBRAS")
     * @return a configured ChatModel instance
     */
    public static ChatModel createChatModel(String provider) {
        return createChatModel(provider, true);
    }
    
    /**
     * Creates a configured ChatModel instance for the specified provider with optional logging.
     * 
     * @param provider the model provider ("OPENAI" or "CEREBRAS")
     * @param enableLogging whether to enable request/response logging
     * @return a configured ChatModel instance
     */
    public static ChatModel createChatModel(String provider, boolean enableLogging) {
        if ("CEREBRAS".equalsIgnoreCase(provider)) {
            return OpenAiChatModel.builder()
                    .baseUrl("https://api.cerebras.ai/v1")
                    .apiKey(System.getenv("CEREBRAS_API_KEY"))
                    .modelName("llama-4-scout-17b-16e-instruct")
                    .logRequests(enableLogging)
                    .logResponses(enableLogging)
                    .build();
        } else {
            // Default to OpenAI
            return OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName(GPT_4_O_MINI)
                    .logRequests(enableLogging)
                    .logResponses(enableLogging)
                    .build();
        }
    }
}
