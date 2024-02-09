import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.azure.AzureOpenAiImageModel;
import dev.langchain4j.model.output.Response;

public class AzureOpenAIDallEExample {
    public static void main(String[] args) {
        AzureOpenAiImageModel model = AzureOpenAiImageModel.builder()
                .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                .deploymentName(System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME"))
                .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                .logRequestsAndResponses(true)
                .build();

        Response<Image> response = model.generate("A coffee mug in Paris, France");

        System.out.println(response.toString());

        Image image = response.content();

        System.out.println("The remote image is here:" + image.url());
    }
}
