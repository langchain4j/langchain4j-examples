import java.time.Duration;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.router,ModelRouter;
import dev.langchain4j.model.router,FailoverStrategy;

public class FailoverStrategyExample {

	public static void main(String[] args) {

        ChatModel firstModel = AzureOpenAiChatModel.builder()
                .apiKey("INVALID")
                .endpoint(System.getenv("FIRST_AZURE_OPENAI_ENDPOINT"))
                .deploymentName(System.getenv("FIRST_AZURE_OPENAI_DEPLOYMENT_NAME"))
                .build();

        ChatModel secondModel = AzureOpenAiChatModel.builder()
                .apiKey(System.getenv("SECOND_AZURE_OPENAI_KEY"))
                .endpoint(System.getenv("SECOND_AZURE_OPENAI_ENDPOINT"))
                .deploymentName(System.getenv("SECOND_AZURE_OPENAI_DEPLOYMENT_NAME"))
                .build();

        ModelRouter router = ModelRouter.builder()
                .addRoutes(firstModel, secondModel)
                .routingStrategy(new FailoverStrategy(Duration.ofMinutes(5)))
                .build();
        assertNotNull(router.chat(new UserMessage("hello")));
    }
}
