import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
     * 2. Run "docker exec -it ollama ollama run llama2" <- specify the desired model here
     */

    static String MODEL_NAME = "orca-mini"; // try "mistral", "llama2", "codellama" or "phi"
    static String DOCKER_IMAGE_NAME = "langchain4j/ollama-" + MODEL_NAME + ":latest";
    static Integer PORT = 11434;

    @Container
    static GenericContainer<?> ollama = new GenericContainer<>(DOCKER_IMAGE_NAME)
            .withExposedPorts(PORT);

    @Test
    void simple_example() {

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .build();

        String joke = model.generate("Tell me a joke about Java");

        System.out.println(joke);
    }

    @Test
    void json_output_example() {

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .format("json")
                .build();

        String json = model.generate("Give me a JSON with 2 fields: name and age of a John Doe, 42");

        System.out.println(json);
    }

    static String baseUrl() {
        return String.format("http://%s:%d", ollama.getHost(), ollama.getMappedPort(PORT));
    }
}
