import dev.langchain4j.data.message.*;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModelName;
import dev.langchain4j.model.anthropic.AnthropicTokenUsage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static dev.langchain4j.internal.Utils.readBytes;
import static org.assertj.core.api.Assertions.assertThat;

class AnthropicChatModelTest {

    ChatLanguageModel model = AnthropicChatModel.builder()
            // API key can be created here: https://console.anthropic.com/settings/keys
            .apiKey(System.getenv("ANTHROPIC_API_KEY"))
            .modelName("claude-3-haiku-20240307")
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

    @Test
    void AnthropicChatModel_with_cache_system_message_Example() {
        ChatLanguageModel modelWithCache = AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .beta("prompt-caching-2024-07-31")
                .modelName(AnthropicChatModelName.CLAUDE_3_HAIKU_20240307)
                .cacheSystemMessages(true)
                .logRequests(true)
                .logResponses(true)
                .build();

        // Now cache has in beta
        // You can send up to 4 systemMessages/Tools

        // create cache
        SystemMessage systemMessage = SystemMessage.from("What types of messages are supported in LangChain?".repeat(187));
        UserMessage userMessage = UserMessage.userMessage("what result it calcule 5x2 + 2x + 2 = 0?");
        Response<AiMessage> response = modelWithCache.generate(systemMessage, userMessage);

        AnthropicTokenUsage createCacheTokenUsage = (AnthropicTokenUsage) response.tokenUsage();
        assertThat(createCacheTokenUsage.cacheCreationInputTokens()).isGreaterThan(0);

        // read cache created
        Response<AiMessage> responseToReadCache = modelWithCache.generate(systemMessage, userMessage);
        AnthropicTokenUsage readCacheTokenUsage = (AnthropicTokenUsage) responseToReadCache.tokenUsage();
        assertThat(readCacheTokenUsage.cacheReadInputTokens()).isGreaterThan(0);
    }
}
