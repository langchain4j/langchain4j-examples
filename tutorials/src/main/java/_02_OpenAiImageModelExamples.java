import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static dev.langchain4j.model.openai.OpenAiImageModelName.GPT_IMAGE_1;

public class _02_OpenAiImageModelExamples {

    public static void main(String[] args) throws IOException {

        ImageModel model = OpenAiImageModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_IMAGE_1)
                .build();

        Response<Image> response = model.generate(
                "Swiss software developers with cheese fondue, a parrot and a cup of coffee");

        // The new GPT image models return the image as base64-encoded data (not a URL).
        Image image = response.content();
        byte[] bytes = Base64.getDecoder().decode(image.base64Data());
        Path path = Files.write(Path.of("swiss-developers.png"), bytes);

        System.out.println("Your image is here: " + path.toAbsolutePath());
    }
}
