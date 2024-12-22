import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import utils.AbstractOllamaInfrastructure;

import java.util.Map;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static org.assertj.core.api.Assertions.assertThat;

class OllamaChatModelTest extends AbstractOllamaInfrastructure {

    /**
     * If you have Ollama running locally,
     * please set the OLLAMA_BASE_URL environment variable (e.g., http://localhost:11434).
     * If you do not set the OLLAMA_BASE_URL environment variable,
     * Testcontainers will download and start Ollama Docker container.
     * It might take a few minutes.
     */

    @Test
    void simple_example() {

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl(ollama))
                .modelName(MODEL_NAME)
                .logRequests(true)
                .build();

        String answer = chatModel.chat("Provide 3 short bullet points explaining why Java is awesome");
        System.out.println(answer);

        assertThat(answer).isNotBlank();
    }

    @Test
    void json_schema_with_AI_Service_example() {

        record Person(String name, int age) {
        }

        interface PersonExtractor {

            Person extractPersonFrom(String text);
        }

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl(ollama))
                .modelName(MODEL_NAME)
                .temperature(0.0)
                .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                .logRequests(true)
                .build();

        PersonExtractor personExtractor = AiServices.create(PersonExtractor.class, chatModel);

        Person person = personExtractor.extractPersonFrom("John Doe is 42 years old");
        System.out.println(person);

        assertThat(person).isEqualTo(new Person("John Doe", 42));
    }

    @Test
    void json_schema_with_low_level_chat_api_example() {

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl(ollama))
                .modelName(MODEL_NAME)
                .temperature(0.0)
                .logRequests(true)
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .messages(UserMessage.from("John Doe is 42 years old"))
                .responseFormat(ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(JsonSchema.builder()
                                .rootElement(JsonObjectSchema.builder()
                                        .addStringProperty("name")
                                        .addIntegerProperty("age")
                                        .build())
                                .build())
                        .build())
                .build();

        ChatResponse chatResponse = chatModel.chat(chatRequest);
        System.out.println(chatResponse);

        assertThat(toMap(chatResponse.aiMessage().text())).isEqualTo(Map.of("name", "John Doe", "age", 42));
    }

    @Test
    void json_schema_with_low_level_model_builder_example() {

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl(ollama))
                .modelName(MODEL_NAME)
                .temperature(0.0)
                .responseFormat(ResponseFormat.builder()
                        .type(ResponseFormatType.JSON)
                        .jsonSchema(JsonSchema.builder()
                                .rootElement(JsonObjectSchema.builder()
                                        .addStringProperty("name")
                                        .addIntegerProperty("age")
                                        .build())
                                .build())
                        .build())
                .logRequests(true)
                .build();

        String json = chatModel.chat("Extract: John Doe is 42 years old");
        System.out.println(json);

        assertThat(toMap(json)).isEqualTo(Map.of("name", "John Doe", "age", 42));
    }

    @Test
    void json_mode_with_low_level_model_builder_example() {

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl(ollama))
                .modelName(MODEL_NAME)
                .temperature(0.0)
                .responseFormat(ResponseFormat.JSON)
                .logRequests(true)
                .build();

        String json = chatModel.chat("Give me a JSON object with 2 fields: name and age of a John Doe, 42");
        System.out.println(json);

        assertThat(toMap(json)).isEqualTo(Map.of("name", "John Doe", "age", 42));
    }

    private static Map<String, Object> toMap(String json) {
        try {
            return new ObjectMapper().readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
