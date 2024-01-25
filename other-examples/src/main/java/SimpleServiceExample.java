import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public class SimpleServiceExample {

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {

        ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder().apiKey(ApiKeys.OPENAI_API_KEY)
                .baseUrl("https://api.baichuan-ai.com/v1").modelName("Baichuan2-Turbo").build();

        Assistant assistant = AiServices.create(Assistant.class, chatLanguageModel);

        String answer = assistant.chat("你好");

        System.out.println(answer); // Hello! How can I assist you today?
    }
}
