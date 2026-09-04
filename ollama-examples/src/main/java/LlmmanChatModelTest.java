import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.junit.jupiter.api.Test;

import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * llmman (https://github.com/llmmanorg/llmman) serves the Ollama API on port 17434, so OllamaChatModel works unchanged.
 * Run {@code llmman serve} and {@code llmman pull gemma4} first; set LLMMAN_HOST ([host][:port]) if it listens elsewhere.
 */
class LlmmanChatModelTest {

    static final String MODEL_NAME = "gemma4";

    @Test
    void simple_example() {

        ChatModel chatModel = OllamaChatModel.builder()
                .baseUrl(llmmanBaseUrl())
                .modelName(MODEL_NAME)
                .logRequests(true)
                .build();

        String answer = chatModel.chat("Provide 3 short bullet points explaining why Java is awesome");
        System.out.println(answer);

        assertThat(answer).isNotBlank();
    }

    static String llmmanBaseUrl() {
        String host = System.getenv("LLMMAN_HOST");
        if (isNullOrEmpty(host)) {
            return "http://localhost:17434";
        }
        return host.startsWith("http") ? host : "http://" + host;
    }
}
