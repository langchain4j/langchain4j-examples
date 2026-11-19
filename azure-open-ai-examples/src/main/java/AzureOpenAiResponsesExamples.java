import com.azure.ai.openai.responses.models.ResponsesReasoningConfigurationEffort;
import dev.langchain4j.model.azure.AzureOpenAiResponsesChatModel;

public class AzureOpenAiResponsesExamples {

    static class Reasoning_Summary {

        public static void main(String[] args) {
            AzureOpenAiResponsesChatModel model = AzureOpenAiResponsesChatModel.builder()
                    .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                    .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                    .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                    .reasoningSummary("auto")
                    .reasoningEffort(ResponsesReasoningConfigurationEffort.MEDIUM)
                    .logRequestsAndResponses(true)
                    .build();

            var response = model.chat(
                    "do 2 plus 12 then 14 divided by 2. then put all of those multiplied by 3. think step by step");

            System.out.println("text: " + response);
            System.out.println("thinking: " + response.thinking());
        }
    }
}
