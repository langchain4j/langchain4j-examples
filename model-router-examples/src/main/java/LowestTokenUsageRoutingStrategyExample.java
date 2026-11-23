import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.router,ModelRouter;
import dev.langchain4j.model.router,LowestTokenUsageRoutingStrategy;

public class LowestTokenUsageRoutingStrategyExample {

	public static void main(String[] args) {
        ChatModel firstModel = AzureOpenAiChatModel.builder()
                .apiKey(System.getenv("FIRST_AZURE_OPENAI_KEY"))
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
                .routingStrategy(new LowestTokenUsageRoutingStrategy())
                .build();
        ChatResponse first = router.chat(new UserMessage("hello"));
        ChatResponse second = router.chat(new UserMessage("hello"));
        assertNotEquals(first.modelName(), second.modelName(), "failed to pick unused model for second call");
        ChatResponse third = router.chat(new UserMessage("hello"));
        assertEquals(first.modelName(), third.modelName(), "failed to pick first model for third call");
    }
}
