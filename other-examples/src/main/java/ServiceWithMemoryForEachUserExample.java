import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class ServiceWithMemoryForEachUserExample {

    /**
     * See also {@link ServiceWithPersistentMemoryForEachUserExample}.
     */

    interface Assistant {

        String chat(@MemoryId int memoryId, @UserMessage String userMessage);
    }

    public static void main(String[] args) {

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();

        System.out.println(assistant.chat(1, "Hello, my name is Klaus"));
        // Hi Klaus! How can I assist you today?

        System.out.println(assistant.chat(2, "Hello, my name is Francine"));
        // Hello Francine! How can I assist you today?

        System.out.println(assistant.chat(1, "What is my name?"));
        // Your name is Klaus.

        System.out.println(assistant.chat(2, "What is my name?"));
        // Your name is Francine.
    }
}