import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class AnthropicStreamingChatModelTest {

    StreamingChatLanguageModel model = AnthropicStreamingChatModel.builder()
            // API key can be created here: https://console.anthropic.com/settings/keys
            .apiKey(System.getenv("ANTHROPIC_API_KEY"))
            .modelName("claude-3-haiku-20240307")
            .logRequests(true)
            // Other parameters can be set as well
            .build();

    @Test
    void AnthropicChatModel_Example() throws ExecutionException, InterruptedException {

        CompletableFuture<AiMessage> future = new CompletableFuture<>();

        model.generate("What is the capital of Germany?", new StreamingResponseHandler<AiMessage>() {

            @Override
            public void onNext(String token) {
                System.out.println("New token: '" + token + "'");
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                System.out.println("Streaming completed: " + response);
                future.complete(response.content());
            }

            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
        });

        assertThat(future.get().text()).containsIgnoringCase("Berlin");
    }
}
