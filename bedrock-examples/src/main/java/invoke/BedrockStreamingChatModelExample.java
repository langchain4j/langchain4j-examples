package invoke;

import dev.langchain4j.model.bedrock.BedrockAnthropicStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import software.amazon.awssdk.regions.Region;

import java.util.concurrent.CompletableFuture;

public class BedrockStreamingChatModelExample {

    public static void main(String[] args) {

        // For authentication, set the following environment variables:
        // AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
        // More info on creating the API keys:
        // https://docs.aws.amazon.com/bedrock/latest/userguide/api-setup.html
        StreamingChatModel model = BedrockAnthropicStreamingChatModel.builder()
                .region(Region.US_EAST_1)
                .model("anthropic.claude-3-5-sonnet-20240620-v1:0")
                .maxTokens(300)
                .temperature(0.50f)
                .maxRetries(1)
                // Other parameters can be set as well
                .build();

        CompletableFuture<ChatResponse> futureChatResponse = new CompletableFuture<>();

        model.chat("Write a poem about Java", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                futureChatResponse.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                futureChatResponse.completeExceptionally(error);
            }
        });

        futureChatResponse.join();
    }
}
