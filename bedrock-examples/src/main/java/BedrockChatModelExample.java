import dev.langchain4j.model.bedrock.BedrockAnthropicMessageChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import software.amazon.awssdk.regions.Region;

public class BedrockChatModelExample {

    public static void main(String[] args) {

        // For authentication, set the following environment variables:
        // AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
        // More info on creating the API keys:
        // https://docs.aws.amazon.com/bedrock/latest/userguide/api-setup.html
        ChatLanguageModel model = BedrockAnthropicMessageChatModel
                .builder()
                .temperature(0.50f)
                .maxTokens(300)
                .region(Region.US_EAST_1)
                .model(BedrockAnthropicMessageChatModel.Types.AnthropicClaude3SonnetV1.getValue())
                .maxRetries(1)
                // Other parameters can be set as well
                .build();

        String joke = model.generate("Tell me a joke about Java");

        System.out.println(joke);
    }
}
