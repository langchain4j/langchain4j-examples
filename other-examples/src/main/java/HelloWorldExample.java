import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class HelloWorldExample {

    public static void main(String[] args) {

        // Create an instance of a model
        ChatLanguageModel model = OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        // Start interacting
        AiMessage answer = model.sendUserMessage("Hello world!");

        System.out.println(answer.text()); // Hello! How can I assist you today?
    }
}
