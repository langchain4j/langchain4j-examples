import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static dev.langchain4j.internal.Utils.readBytes;
import static org.assertj.core.api.Assertions.assertThat;

class AnthropicChatModelTest {

    AnthropicChatModel model = AnthropicChatModel.builder()
            // API key can be created here: https://console.anthropic.com/settings/keys
            .apiKey(System.getenv("ANTHROPIC_API_KEY"))
            .modelName("claude-3-sonnet-20240229")
            .logRequests(true)
            .logResponses(true)
            // Other parameters can be set as well
            .build();

    @Test
    void AnthropicChatModel_Example() {

        String answer = model.generate("What is the capital of Germany?");

        assertThat(answer).containsIgnoringCase("Berlin");
    }

    @Test
    void AnthropicChatModel_with_vision_Example() {

        byte[] image = readBytes("https://docs.langchain4j.dev/img/langchain4j-components.png");

        UserMessage userMessage = UserMessage.from(
                TextContent.from("What do you see?"),
                ImageContent.from(Base64.getEncoder().encodeToString(image), "image/png")
        );

        Response<AiMessage> response = model.generate(userMessage);

        assertThat(response.content().text()).containsIgnoringCase("RAG");
    }
}
