import java.util.List;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.watsonx.WatsonxTokenCountEstimator;

public class WatsonxTokenCounterEstimatorTest {

    public static void main(String... args) throws Exception {

        try {
            
        TokenCountEstimator tokenCounterEstimator = WatsonxTokenCountEstimator.builder()
            .url(System.getenv("WATSONX_URL"))
            .apiKey(System.getenv("WATSONX_API_KEY"))
            .projectId(System.getenv("WATSONX_PROJECT_ID"))
            .modelName("ibm/granite-3-3-8b-instruct")
            .build();

        var toolExecutionRequest = ToolExecutionRequest.builder()
            .id("id")
            .name("sum")
            .arguments("{ \"firstNumber\": 1, \"secondNumber\": 2 }")
            .build();

        Iterable<ChatMessage> messages = List.of(
            SystemMessage.from("You are an helpful assistant."),
            UserMessage.from("John", "What is the date today?"),
            AiMessage.aiMessage("Today is 2025-03-20"),
            UserMessage.from("John", "Can you execute 2 + 2"),
            AiMessage.aiMessage(toolExecutionRequest),
            ToolExecutionResultMessage.from(toolExecutionRequest, "4")
        );

        int count = tokenCounterEstimator.estimateTokenCountInMessages(messages);
        System.out.println(count);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}
