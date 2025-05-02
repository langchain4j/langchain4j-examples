import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class HelloWorldExample {

    public static void main(String[] args) {

        // Create an instance of a model
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();

        // Start interacting
        String answer = model.chat("Hello world!");

        System.out.println(answer); // Hello! How can I assist you today?
    }
}
