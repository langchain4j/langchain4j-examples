import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class AnthropicStreamingChatModelTest {

    StreamingChatModel model = AnthropicStreamingChatModel.builder()
            // API key can be created here: https://console.anthropic.com/settings/keys
            .apiKey(System.getenv("ANTHROPIC_API_KEY"))
            .modelName("claude-3-haiku-20240307")
            .logRequests(true)
            // Other parameters can be set as well
            .build();

    @Test
    void AnthropicChatModel_Example() throws ExecutionException, InterruptedException {

        CompletableFuture<ChatResponse> future = new CompletableFuture<>();

        model.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.println("New token: '" + partialResponse + "'");
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                System.out.println("Streaming completed: " + completeResponse);
                future.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
        });

        assertThat(future.get().aiMessage().text()).containsIgnoringCase("Berlin");
    }
}
