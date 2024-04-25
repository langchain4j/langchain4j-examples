package dev.langchain4j.example.chat;

import static java.time.Duration.ofSeconds;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChatAgent {

    @Inject
    @ConfigProperty(name = "hugging.face.api.key")
    private String HUGGING_FACE_API_KEY;

    @Inject
    @ConfigProperty(name = "chat.model.id")
    private String CHAT_MODEL_ID;

    @Inject
    @ConfigProperty(name = "chat.model.timeout")
    private Integer TIMEOUT;

    @Inject
    @ConfigProperty(name = "chat.model.max.token")
    private Integer MAX_NEW_TOKEN;

    @Inject
    @ConfigProperty(name = "chat.model.temperature")
    private Double TEMPERATURE;

    @Inject
    @ConfigProperty(name = "chat.memory.max.messages")
    private Integer MAX_MESSAGES;

    interface Assistant {
       String chat(@MemoryId String sessionId, @UserMessage String userMessage);
    }

    private Assistant assistant = null;

    public Assistant getAssistant() {
        if (assistant == null) {
            HuggingFaceChatModel model = HuggingFaceChatModel.builder()
                .accessToken(HUGGING_FACE_API_KEY)
                .modelId(CHAT_MODEL_ID)
                .timeout(ofSeconds(TIMEOUT))
                .temperature(TEMPERATURE)
                .maxNewTokens(MAX_NEW_TOKEN)
                .waitForModel(true)
                .build();
            assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(
                    sessionId -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGES))
                .build();
        }
        return assistant;
    }

    public String chat(String sessionId, String message) {
        return getAssistant().chat(sessionId, message).trim();
    }

}
