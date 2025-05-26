package dev.langchain4j.example.chat;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.example.chat.util.ModelBuilder;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChatAgent {

    @Inject
    private ModelBuilder modelBuilder;

    @Inject
    @ConfigProperty(name = "chat.memory.max.messages")
    private Integer MAX_MESSAGES;

    interface Assistant {
       String chat(@MemoryId String sessionId, @UserMessage String userMessage);
    }

    private Assistant assistant = null;

    public Assistant getAssistant() throws Exception {
        if (assistant == null) {
            ChatModel model = modelBuilder.getChatModelForWeb();
            assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemoryProvider(
                    sessionId -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGES))
                .build();
        }
        return assistant;
    }

    public String chat(String sessionId, String message) throws Exception {
        String reply = getAssistant().chat(sessionId, message).trim();
        int i = reply.lastIndexOf(message);
        return i > 0 ? reply.substring(i) : reply;
    }

}
