import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.tinylog.Logger;

@Testcontainers
class OllamaChatModelTest {

    /**
     * The first time you run this test, it will download a Docker image with Ollama and a model.
     * It might take a few minutes.
     * <p>
     * This test uses modified Ollama Docker images, which already contain models inside them.
     * All images with pre-packaged models are available here: https://hub.docker.com/repositories/langchain4j
     * <p>
     * However, you are not restricted to these images.
     * You can run any model from https://ollama.ai/library by following these steps:
     * 1. Run "docker run -d -v ollama:/root/.ollama -p 11434:11434 --name ollama ollama/ollama"
     * 2. Run "docker exec -it ollama ollama run mistral" <- specify the desired model here
     */

    @Container
    private static GenericContainer<?> ollama = new GenericContainer<>(OllamaContants.OLLAMA_IMAGE_NAME).withExposedPorts(OllamaContants.OLLAMA_PORT);

    @Test
    void example() {
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl(ollama))
                .modelName(OllamaContants.MODEL_NAME)
                .build();
        simpleExample(model);
        jsonOutputExample(model);
    }

    void simpleExample(ChatLanguageModel model) {
        String answer = model.generate("Provide 3 short bullet points explaining why Java is awesome");
        Logger.info("Answer: {}", answer);
    }

    void jsonOutputExample(ChatLanguageModel model) {
        String json = model.generate("Give me a JSON with 2 fields: name and age of a John Doe, 42");
        Logger.info("JSON: {}", json);
    }

    static String baseUrl(GenericContainer<?> ollama) {
        return String.format("http://%s:%d", ollama.getHost(), ollama.getFirstMappedPort());
    }
}
