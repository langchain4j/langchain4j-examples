import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static java.time.Duration.ofSeconds;

public class _1__ModelParameters {

    public static void main(String[] args) {

        // TODO find lists of parameter possibilities (in OpenAiModelName) temperature
        // means wath etcs

        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_3_5_TURBO)
                .temperature(0.3)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        String prompt = "Explain in three lines how to make a beautiful painting";

        String response = model.generate(prompt);

        System.out.println(response);
    }
}
