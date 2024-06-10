import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;

public class OpenAiImageModelExamples {

    public static void main(String[] args) {

        ImageModel model = OpenAiImageModel.withApiKey(System.getenv("OPENAI_API_KEY"));

        Response<Image> response = model.generate("Donald Duck in New York, cartoon style");

        System.out.println(response.content().url()); // Donald Duck is here :)
    }
}
