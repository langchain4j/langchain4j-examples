import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.router.DecisionModelChatModelRouter;
import dev.langchain4j.model.chat.router.RoutingChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import java.time.Duration;

public class DrexModelRoutingExample {

    interface Assistant {
        Result<String> chat(String message);
    }

    public static void main(String[] args) {
        DecisionModel decisionModel = DrexDecisionExamples.decisionModel();
        String openAiKey = DrexDecisionExamples.requiredEnvironment("OPENAI_API_KEY");
        ChatModel smallModel = OpenAiChatModel.builder()
                .apiKey(openAiKey)
                .modelName(DrexDecisionExamples.requiredEnvironment("SMALL_CHAT_MODEL"))
                .timeout(Duration.ofSeconds(60))
                .maxRetries(0)
                .build();
        ChatModel largeModel = OpenAiChatModel.builder()
                .apiKey(openAiKey)
                .modelName(DrexDecisionExamples.requiredEnvironment("LARGE_CHAT_MODEL"))
                .timeout(Duration.ofSeconds(60))
                .maxRetries(0)
                .build();

        Assistant assistant = AiServices.create(
                Assistant.class, routedModel(decisionModel, smallModel, largeModel));
        String question = args.length == 0 ? "How do I reset my password?" : String.join(" ", args);
        Result<String> result = assistant.chat(question);

        System.out.println("Selected route: "
                + result.finalResponse().aiMessage().attributes().get(RoutingChatModel.ROUTE_ATTRIBUTE));
        System.out.println(result.content());
        System.out.println("Generation token usage (excludes routing): " + result.tokenUsage());
    }

    static ChatModel routedModel(DecisionModel decisionModel, ChatModel smallModel, ChatModel largeModel) {
        return RoutingChatModel.builder()
                .route("simple", "Simple support questions and factual lookups", smallModel)
                .route("complex", "Multi-step troubleshooting and detailed analysis", largeModel)
                .router(new DecisionModelChatModelRouter(decisionModel))
                .defaultRoute("complex")
                .build();
    }
}
