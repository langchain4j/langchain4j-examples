import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;

public class MistralAiChatModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            ChatLanguageModel model = MistralAiChatModel.builder()
                    .apiKey(System.getenv("MISTRAL_AI_API_KEY")) // Please use your own Mistral AI API key
                    .modelName("mistral-small")
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            String joke = model.generate("Tell me a joke about Java");

            System.out.println(joke);
        }
    }
}
