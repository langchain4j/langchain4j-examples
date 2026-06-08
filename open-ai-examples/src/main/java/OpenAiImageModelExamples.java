import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static dev.langchain4j.model.openai.OpenAiImageModelName.GPT_IMAGE_1;

public class OpenAiImageModelExamples {

    public static void main(String[] args) throws IOException {

        ImageModel model = OpenAiImageModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_IMAGE_1)
                .quality("low")
                .build();

        Response<Image> response = model.generate("Donald Duck in New York, cartoon style");

        // The new GPT image models return the image as base64-encoded data (not a URL).
        Image image = response.content();
        byte[] bytes = Base64.getDecoder().decode(image.base64Data());
        Path path = Files.write(Path.of("donald-duck.png"), bytes);

        System.out.println("Donald Duck is here: " + path.toAbsolutePath()); // :)
    }
}
