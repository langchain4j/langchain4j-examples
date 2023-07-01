import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public class ServiceWithMemoryExample {

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {

        String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys
        ChatLanguageModel chatLanguageModel = OpenAiChatModel.withApiKey(apiKey);

        ChatMemory chatMemory = MessageWindowChatMemory.withCapacity(10);

        SimpleServiceExample.Assistant assistant = AiServices.builder(SimpleServiceExample.Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .build();

        String answer = assistant.chat("Hello! My name is Klaus.");
        System.out.println(answer); // Hello Klaus! How can I assist you today?

        String answerWithName = assistant.chat("What is my name?");
        System.out.println(answerWithName); // Your name is Klaus.
    }
}
