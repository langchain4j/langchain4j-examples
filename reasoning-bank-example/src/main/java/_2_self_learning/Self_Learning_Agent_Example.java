package _2_self_learning;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.reasoning.InMemoryReasoningBank;
import dev.langchain4j.reasoning.ReasoningStrategy;
import dev.langchain4j.reasoning.ReasoningTrace;
import dev.langchain4j.reasoning.SimpleReasoningDistiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Self_Learning_Agent_Example {

    private static final Logger log = LoggerFactory.getLogger(Self_Learning_Agent_Example.class);

    /**
     * This example demonstrates the self-learning workflow of ReasoningBank:
     * <p>
     * 1. Capture reasoning traces from task executions
     * 2. Distill successful traces into reusable strategies
     * 3. Store strategies in the ReasoningBank
     * 4. Future tasks benefit from learned strategies
     * <p>
     * This implements the core concept from the paper "Scaling Agent Self-Evolving
     * with Reasoning Memory" (arXiv:2509.25140).
     */

    public static void main(String[] args) {

        // Create our components
        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();
        InMemoryReasoningBank reasoningBank = new InMemoryReasoningBank();
        SimpleReasoningDistiller distiller = SimpleReasoningDistiller.builder()
                .baseConfidence(0.7)
                .learnFromFailures(true)  // Also learn what NOT to do from failures
                .build();


        log.info("=== Phase 1: Capturing Reasoning Traces ===");
        log.info("In a real application, these would be captured during actual task execution.");
        log.info("");

        // Simulate capturing reasoning traces from past task executions
        // In a real application, you would capture these during actual LLM interactions

        // Trace 1: Successful math problem solving
        ReasoningTrace mathTrace1 = ReasoningTrace.successful(
                "Solve the quadratic equation: x^2 - 5x + 6 = 0",
                "I'll factor this quadratic. Looking for two numbers that multiply to 6 " +
                        "and add to -5. Those are -2 and -3. So (x-2)(x-3) = 0, giving x = 2 or x = 3.",
                "x = 2 or x = 3"
        );

        // Trace 2: Another successful math problem
        ReasoningTrace mathTrace2 = ReasoningTrace.successful(
                "Solve: 3x + 7 = 22",
                "Subtract 7 from both sides: 3x = 15. Divide both sides by 3: x = 5.",
                "x = 5"
        );

        // Trace 3: A failed attempt (we can learn from failures too!)
        ReasoningTrace failedTrace = ReasoningTrace.builder()
                .taskDescription("Solve: x^2 + 1 = 0 in real numbers")
                .thinking("Tried to factor but x^2 + 1 cannot be factored over real numbers. " +
                        "This equation has no real solutions.")
                .successful(false)
                .build();

        // Trace 4: Successful API debugging
        ReasoningTrace apiTrace = ReasoningTrace.successful(
                "Debug: API returning 401 Unauthorized",
                "Checked the request headers and found the Authorization token was expired. " +
                        "The token refresh logic wasn't being triggered. Fixed by adding a " +
                        "token expiry check before each request.",
                "Added token validation and refresh logic before API calls"
        );

        List<ReasoningTrace> traces = List.of(mathTrace1, mathTrace2, failedTrace, apiTrace);

        log.info("Captured {} reasoning traces", traces.size());
        log.info("");


        log.info("=== Phase 2: Distilling Strategies ===");
        log.info("Converting successful traces into reusable strategies...");
        log.info("");

        // Distill traces into strategies
        // The distiller will:
        // - Extract successful approaches from positive traces
        // - Optionally learn pitfalls from failed traces
        // - Assign confidence scores based on success rate
        List<ReasoningStrategy> strategies = distiller.distillAll(traces);

        log.info("Distilled {} strategies from traces", strategies.size());

        for (ReasoningStrategy strategy : strategies) {
            log.info("Strategy for '{}': {}", strategy.taskPattern(),
                    strategy.strategy().substring(0, Math.min(50, strategy.strategy().length())) + "...");
        }
        log.info("");


        log.info("=== Phase 3: Storing in ReasoningBank ===");
        log.info("Storing strategies with embeddings for similarity search...");
        log.info("");

        // Store each strategy with its embedding
        for (ReasoningStrategy strategy : strategies) {
            Embedding embedding = embeddingModel.embed(strategy.taskPattern()).content();
            String id = reasoningBank.store(strategy, embedding);
            log.info("Stored strategy with ID: {}", id);
        }

        log.info("");
        log.info("ReasoningBank now contains {} strategies", reasoningBank.size());
        log.info("");


        log.info("=== Phase 4: Retrieving Relevant Strategies ===");
        log.info("Simulating retrieval for new tasks...");
        log.info("");

        // Simulate querying the ReasoningBank for a new task
        String newTask = "Solve the equation: x^2 - 4x + 4 = 0";
        Embedding queryEmbedding = embeddingModel.embed(newTask).content();

        var result = reasoningBank.retrieve(queryEmbedding, 2);

        log.info("Query: '{}'", newTask);
        log.info("Found {} relevant strategies:", result.size());

        for (var match : result.matches()) {
            log.info("  - Score: {:.2f} - Task Pattern: '{}'",
                    match.score(), match.strategy().taskPattern());
            log.info("    Strategy: {}", match.strategy().strategy());
        }


        log.info("");
        log.info("=== Self-Learning Complete ===");
        log.info("The ReasoningBank is now ready to augment future tasks with learned strategies.");
        log.info("Use ReasoningAugmentor with AiServices to automatically inject relevant strategies.");
    }
}
