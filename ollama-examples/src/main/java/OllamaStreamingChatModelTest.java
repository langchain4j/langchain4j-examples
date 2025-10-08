import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
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

        StreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl(ollamaBaseUrl(ollama))
                .modelName(MODEL_NAME)
                .build();

        String userMessage = "Write a 100-word poem about Java and AI";

        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        model.chat(userMessage, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                futureResponse.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                futureResponse.completeExceptionally(error);
            }
        });

        futureResponse.join();
    }
}
