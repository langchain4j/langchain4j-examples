import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class HelloWorldExample {

    public static void main(String[] args) {

        // Create an instance of a model
        ChatLanguageModel model = OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        // Start interacting
        String answer = model.generate("Hello world!");

        System.out.println(answer); // Hello! How can I assist you today?
    }
}
