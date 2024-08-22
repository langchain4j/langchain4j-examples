import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;

public class _02_OpenAiImageModelExamples {

    public static void main(String[] args) {

        ImageModel model = OpenAiImageModel.builder()
                .modelName("dall-e-3")
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();

        Response<Image> response = model.generate(
                "Swiss software developers with cheese fondue, a parrot and a cup of coffee");

        System.out.println(response.content().url());
    }
}
