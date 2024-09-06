import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_3_5_TURBO;

public class HelloWorldExample {

    public static void main(String[] args) {

        // Create an instance of a model
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_3_5_TURBO)
                .build();

        // Start interacting
        String answer = model.generate("Hello world!");

        System.out.println(answer); // Hello! How can I assist you today?
    }
}
