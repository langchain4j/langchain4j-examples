package util;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class ChatModelProvider {
    
    public static ChatModel createChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
    
    public static ChatModel createChatModel(boolean enableLogging) {
        return createChatModel("OPENAI", enableLogging);
    }
    
    public static ChatModel createChatModel(String provider) {
        return createChatModel(provider, true);
    }
    
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
            return OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName(GPT_4_O_MINI)
                    .logRequests(enableLogging)
                    .logResponses(enableLogging)
                    .build();
        }
    }
}