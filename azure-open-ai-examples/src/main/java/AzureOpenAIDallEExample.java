import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.azure.AzureOpenAiImageModel;
import dev.langchain4j.model.output.Response;

public class AzureOpenAIDallEExample {

    static class Simple_Image {

        public static void main(String[] args) {

            AzureOpenAiImageModel model = AzureOpenAiImageModel.builder()
                    .apiKey(System.getenv("AZURE_OPENAI_KEY"))
                    .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
                    .deploymentName(System.getenv("AZURE_OPENAI_DALLE_DEPLOYMENT_NAME"))
                    .logRequestsAndResponses(true)
                    .build();

            Response<Image> response = model.generate("A coffee mug in Paris, France");

            System.out.println(response.toString());

            Image image = response.content();

            System.out.println("The remote image is here:" + image.url());
        }
    }
}
