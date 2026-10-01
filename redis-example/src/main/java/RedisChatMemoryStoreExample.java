import com.redis.testcontainers.RedisStackContainer;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

import java.util.List;

import static com.redis.testcontainers.RedisStackContainer.DEFAULT_IMAGE_NAME;
import static com.redis.testcontainers.RedisStackContainer.DEFAULT_TAG;

public class RedisChatMemoryStoreExample {

    interface Assistant {

        String chat(String message);
    }

    public static final String GEMINI_API_KEY = "YOUR_API_KEY_HERE"; // Replace with your actual Gemini API key

    public static void main(String[] args) {
        RedisStackContainer redis = new RedisStackContainer(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
        redis.start();

        RedisChatMemoryStore redisChatMemoryStore = RedisChatMemoryStore.builder()
                .host(redis.getHost())
                .port(redis.getFirstMappedPort())
                .build();

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(GEMINI_API_KEY)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .build();

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(30)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();

        String answer = assistant.chat("Hello! My name is Homer Simpson.");
        System.out.println(answer); // Hello Homer...

        List<ChatMessage> messages = chatMemory.messages();
        System.out.println(String.format("Chat memory size: %s contents: ", messages.size()));

        for (ChatMessage message : messages) {
            System.out.println(message);
        }

        redis.stop();
    }
}
