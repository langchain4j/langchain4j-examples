import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;
import utils.AbstractOllamaInfrastructure;

import java.util.concurrent.CompletableFuture;

@Testcontainers
class OllamaStreamingChatModelTest extends AbstractOllamaInfrastructure {

    /**
     * If you have Ollama running locally,
     * please set the OLLAMA_BASE_URL environment variable (e.g., http://localhost:11434).
     * If you do not set the OLLAMA_BASE_URL environment variable,
     * Testcontainers will download and start Ollama Docker container.
     * It might take a few minutes.
     */

    @Test
    void streaming_example() {

        StreamingChatLanguageModel model = OllamaStreamingChatModel.builder()
                .baseUrl(ollamaBaseUrl(ollama))
                .modelName(MODEL_NAME)
                .build();

        String userMessage = "Write a 100-word poem about Java and AI";

        CompletableFuture<Response<AiMessage>> futureResponse = new CompletableFuture<>();
        model.generate(userMessage, new StreamingResponseHandler<>() {

            @Override
            public void onNext(String token) {
                System.out.print(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                futureResponse.complete(response);
            }

            @Override
            public void onError(Throwable error) {
                futureResponse.completeExceptionally(error);
            }
        });

        futureResponse.join();
    }
}
