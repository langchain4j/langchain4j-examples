import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class AzureOpenAiChatModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            AzureOpenAiChatModel model = AzureOpenAiChatModel.builder()
                    .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                    .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                    .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                    .temperature(0.3)
                    .logRequestsAndResponses(true)
                    .build();

            String response = model.chat("Provide 3 short bullet points explaining why Java is awesome");

            System.out.println(response);
        }
    }
}
