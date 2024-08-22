import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.bedrock.BedrockAnthropicMessageChatModel;
import dev.langchain4j.model.bedrock.BedrockAnthropicStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import software.amazon.awssdk.regions.Region;

public class BedrockStreamingChatModelExample {

    public static void main(String[] args) {

        // For authentication, set the following environment variables:
        // AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
        // More info on creating the API keys:
        // https://docs.aws.amazon.com/bedrock/latest/userguide/api-setup.html
        StreamingChatLanguageModel model = BedrockAnthropicStreamingChatModel
                .builder()
                .temperature(0.50f)
                .maxTokens(300)
                .region(Region.US_EAST_1)
                .model(BedrockAnthropicMessageChatModel.Types.AnthropicClaudeV2.getValue())
                .maxRetries(1)
                // Other parameters can be set as well
                .build();

        model.generate("Write a poem about Java", new StreamingResponseHandler<AiMessage>() {

            @Override
            public void onNext(String token) {
                System.out.println("onNext(): " + token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                System.out.println("onComplete(): " + response);
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });
    }
}
