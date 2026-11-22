import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.ChoiceQuestion;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.ScaleQuestion;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;
import java.time.Duration;
import java.util.Map;

public class DrexDecisionExamples {

    static final String MODEL = "drex-max-v1.1";
    static final String CLIENT = "langchain4j-nace-examples/1.0";

    public static void main(String[] args) {
        String ticket = args.length == 0
                ? "I was charged twice. Please refund one charge."
                : String.join(" ", args);

        var response = decisionModel().decide(triageRequest(ticket));
        System.out.println("Refund probability: " + response.yesNo("refund").probability());
        System.out.println("Team: " + response.choice("team").value());
        System.out.println("Team probabilities: " + response.choice("team").probabilities());
        System.out.println("Urgency mean (0=low, 1=medium, 2=high): " + response.scale("urgency").mean());
        System.out.println("Urgency probabilities: " + response.scale("urgency").probabilities());
        System.out.println("Decision token usage: " + response.tokenUsage());
    }

    static DecisionModel decisionModel() {
        return decisionModel("https://console.nace.ai", requiredEnvironment("NACE_API_KEY"));
    }

    // An explicit destination/key pair also lets the tests use a local HTTP server.
    static DecisionModel decisionModel(String baseUrl, String apiKey) {
        return new TypeSafeDecisionModel(TypeSafeDecisionModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(MODEL)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(0)
                .customHeaders(Map.of("X-Nace-Client", CLIENT, "User-Agent", CLIENT))) {
            @Override
            public ModelProvider provider() {
                // Reuse the System One transport without attributing Nace calls to TypeSafe.
                return ModelProvider.OTHER;
            }
        };
    }

    static DecisionRequest triageRequest(String ticket) {
        return DecisionRequest.builder()
                .input(ticket)
                .question("refund", YesNoQuestion.from("Is the customer asking for a refund?"))
                .question("team", ChoiceQuestion.builder()
                        .text("Which team should handle this?")
                        .option("billing", "Payments and refunds")
                        .option("support", "Technical problems")
                        .build())
                .question("urgency", ScaleQuestion.builder()
                        .text("How urgent is this?")
                        .level("low")
                        .level("medium")
                        .level("high")
                        .build())
                .build();
    }

    static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set " + name + " before running this example");
        }
        return value;
    }
}
