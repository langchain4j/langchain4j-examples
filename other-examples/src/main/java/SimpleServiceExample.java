import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public class SimpleServiceExample {

    interface Assistant {

        String chat(String message);
    }

    public static void main(String[] args) {

        String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys
        ChatLanguageModel chatLanguageModel = OpenAiChatModel.withApiKey(apiKey);

        Assistant assistant = AiServices.create(Assistant.class, chatLanguageModel);

        String answer = assistant.chat("Hello");

        System.out.println(answer); // Hello! How can I assist you today?
    }
}
