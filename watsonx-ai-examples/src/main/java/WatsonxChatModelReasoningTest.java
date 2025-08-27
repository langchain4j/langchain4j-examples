import java.time.Duration;
import java.util.List;
import com.ibm.watsonx.ai.chat.model.ExtractionTags;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.watsonx.WatsonxChatModel;

class WatsonxChatModelReasoningTest {

    public static void main(String... args) {

        try {
            
            ChatModel chatModel = WatsonxChatModel.builder()
                .url(System.getenv("WATSONX_URL"))
                .apiKey(System.getenv("WATSONX_API_KEY"))
                .projectId(System.getenv("WATSONX_PROJECT_ID"))
                .modelName("ibm/granite-3-3-8b-instruct")
                .timeLimit(Duration.ofSeconds(30))
                .thinking(ExtractionTags.of("think", "response"))
                .build();

            List<ChatMessage> messages = List.of(
                UserMessage.userMessage("Why the sky is blue?")
            );

            ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .maxOutputTokens(0)
                .build();

            var aiMessage = chatModel.chat(chatRequest).aiMessage();

            System.out.println("""
                -----------------------------------
                |            THINKING             |
                -----------------------------------
                %s""".formatted(aiMessage.thinking()));

            System.out.println("""
                -----------------------------------
                |            RESPONSE             |
                -----------------------------------
                %s""".formatted(aiMessage.text()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
