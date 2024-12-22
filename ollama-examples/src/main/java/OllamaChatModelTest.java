import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static dev.langchain4j.model.chat.request.ResponseFormat.JSON;

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

    static String MODEL_NAME = "orca-mini"; // try "mistral", "llama2", "codellama", "phi" or "tinyllama"

    @Container
    static GenericContainer<?> ollama = new GenericContainer<>("langchain4j/ollama-" + MODEL_NAME + ":latest")
            .withExposedPorts(11434);

    @Test
    void simple_example() {

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .build();

        String answer = model.generate("Provide 3 short bullet points explaining why Java is awesome");

        System.out.println(answer);
    }

    @Test
    void json_output_example() {

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .responseFormat(JSON)
                .build();

        String json = model.generate("Give me a JSON with 2 fields: name and age of a John Doe, 42");

        System.out.println(json);
    }

    @Test
    void json_schema_builder_example() {

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .responseFormat(ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(JsonSchema.builder()
                                .name("Person")
                                .rootElement(JsonObjectSchema.builder()
                                        .addStringProperty("fullName")
                                        .addIntegerProperty("age")
                                        .build())
                                .build())
                        .build())
                .build();

        String json = model.generate("Extract data: John Doe, 42");

        System.out.println(json);
    }

    @Test
    void json_schema_chat_api_example() {

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .build();


        ChatResponse chatResponse = model.chat(ChatRequest.builder()
                .messages(UserMessage.from("Extract data: John Doe, 42"))
                .responseFormat(ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(JsonSchema.builder()
                                .name("Person")
                                .rootElement(JsonObjectSchema.builder()
                                        .addStringProperty("fullName")
                                        .addIntegerProperty("age")
                                        .build())
                                .build())
                        .build())
                .build());

        System.out.println(chatResponse.aiMessage().text());
    }


    @Test
    void ollama_tools_specification_example() {

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .build();


        List<ToolSpecification> toolSpecificationList = List.of(
                ToolSpecification.builder()
                        .name("get_fav_color")
                        .description("Gets favorite color of user by ID")
                        .parameters(JsonObjectSchema.builder()
                                .addIntegerProperty("user_id")
                                .required("user_id")
                                .build())
                        .build()
        );

        Response<AiMessage> aiMessageResponse = model.generate(
                List.of(UserMessage.from("Find the favorite color of user Jim with ID 21")),
                toolSpecificationList
        );

        System.out.println(aiMessageResponse.content().toolExecutionRequests());
    }

    static String baseUrl() {
        return String.format("http://%s:%d", ollama.getHost(), ollama.getFirstMappedPort());
    }
}
