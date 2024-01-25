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

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder().apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.baichuan-ai.com/v1").modelName("Baichuan2-Turbo").build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .build();

        String answer = assistant.chat("你好! 我叫李明");
        System.out.println(answer); // Hello Klaus! How can I assist you today?

        String answerWithName = assistant.chat("我的名字叫什么");
        System.out.println(answerWithName); // 你的名字是李明。
    }
}
