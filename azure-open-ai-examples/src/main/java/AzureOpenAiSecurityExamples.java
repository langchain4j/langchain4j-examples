import com.azure.identity.DefaultAzureCredentialBuilder;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;

/**
 * This sample demonstrates how to secure Azure OpenAI API models.
 * To run this sample, please execute the script deploy-azure-openai-security.sh.
 */
public class AzureOpenAiSecurityExamples {

    /**
     * This sample demonstrates that accessing a model using an API key does not work.
     * You should get the following error message:
     * Status code 403, "{"error":{"code":"AuthenticationTypeDisabled","message": "Key based authentication is disabled for this resource."}}"
     */
    static class ApiKey_Example {

        public static void main(String[] args) {

            AzureOpenAiChatModel model = AzureOpenAiChatModel.builder()
                    .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                    .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                    .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                    .temperature(0.3)
                    .logRequestsAndResponses(true)
                    .build();

            String response = model.generate("Provide 3 short bullet points explaining why Java is awesome");

            System.out.println(response);
        }
    }

    /**
     * This sample demonstrates that you need to use Azure Credentials (DefaultAzureCredentialBuilder) instead of an API Key.
     * DefaultAzureCredential combines credentials that are commonly used to authenticate when deployed, with credentials that are used to authenticate in a development environment. 
     */
    static class Azure_Credential_Example {

        public static void main(String[] args) {

            AzureOpenAiChatModel model = AzureOpenAiChatModel.builder()
                    .tokenCredential(new DefaultAzureCredentialBuilder().build())
                    .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                    .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                    .temperature(0.3)
                    .logRequestsAndResponses(true)
                    .build();

            String response = model.generate("Provide 3 short bullet points explaining why Java is awesome");

            System.out.println(response);
        }
    }
}
