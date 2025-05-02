import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static dev.langchain4j.model.anthropic.AnthropicChatModelName.CLAUDE_3_HAIKU_20240307;
import static org.assertj.core.api.Assertions.assertThat;

class AnthropicToolsTest {

    ChatModel model = AnthropicChatModel.builder()
            // API key can be created here: https://console.anthropic.com/settings/keys
            .apiKey(System.getenv("ANTHROPIC_API_KEY"))
            .modelName(CLAUDE_3_HAIKU_20240307)
            .logRequests(true)
            .logResponses(true)
            // Other parameters can be set as well
            .build();

    static class Tools {

        @Tool
        LocalDate currentDate() {
            System.out.println("Called currentDate()");
            return LocalDate.now();
        }

        @Tool
        LocalTime currentTime() {
            System.out.println("Called currentTime()");
            return LocalTime.now();
        }
    }

    interface AiService {

        String chat(String userMessage);
    }

    @Test
    void AnthropicChatModel_Tools_Example() {

        AiService aiService = AiServices.builder(AiService.class)
                .chatModel(model)
                .tools(new Tools())
                .build();

        String answer = aiService.chat("What is the date today?");
        System.out.println(answer);

        assertThat(answer).contains(String.valueOf(LocalDate.now().getDayOfMonth()));
    }
}
