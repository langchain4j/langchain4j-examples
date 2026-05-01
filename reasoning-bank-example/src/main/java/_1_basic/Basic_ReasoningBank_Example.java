package _1_basic;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.reasoning.InMemoryReasoningBank;
import dev.langchain4j.reasoning.ReasoningAugmentor;
import dev.langchain4j.reasoning.ReasoningStrategy;
import dev.langchain4j.service.AiServices;
import shared.Assistant;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static shared.Utils.OPENAI_API_KEY;
import static shared.Utils.startConversationWith;

public class Basic_ReasoningBank_Example {

    /**
     * This example demonstrates how to use ReasoningBank to augment prompts with
     * reasoning strategies learned from past experiences.
     * <p>
     * ReasoningBank is conceptually similar to RAG, but instead of retrieving
     * documents/content (WHAT information to use), it retrieves reasoning strategies
     * (HOW to approach a task).
     * <p>
     * In this basic example, we pre-populate the ReasoningBank with some strategies
     * and show how they are automatically injected when relevant.
     */

    public static void main(String[] args) {

        // Let's create an assistant with ReasoningBank capabilities
        Assistant assistant = createAssistant();

        // Now, let's start the conversation with the assistant. Try queries like:
        // - Solve the equation: 2x + 5 = 15
        // - Debug this error: NullPointerException at line 42
        // - Review this code and find issues
        startConversationWith(assistant);
    }

    private static Assistant createAssistant() {

        // First, let's create a chat model (LLM) that will answer our queries.
        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();

        // We need an embedding model to convert text into vectors for similarity search.
        // We use a local in-process model for simplicity.
        EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();


        // Create an in-memory ReasoningBank to store our reasoning strategies.
        // In production, you might use a persistent store or an embedding store-backed implementation.
        InMemoryReasoningBank reasoningBank = new InMemoryReasoningBank();


        // Let's populate the ReasoningBank with some pre-defined strategies.
        // These represent successful approaches learned from past experiences.

        // Strategy for mathematical problems
        ReasoningStrategy mathStrategy = ReasoningStrategy.builder()
                .taskPattern("mathematical equations and algebra problems")
                .strategy("1. Identify the variable to solve for\n" +
                        "2. Isolate the variable by performing inverse operations on both sides\n" +
                        "3. Simplify step by step, showing your work\n" +
                        "4. Verify the solution by substituting back into the original equation")
                .pitfallsToAvoid("Don't forget to apply operations to both sides equally. " +
                        "Watch out for sign errors when moving terms.")
                .confidenceScore(0.9)
                .build();

        // Strategy for debugging
        ReasoningStrategy debugStrategy = ReasoningStrategy.builder()
                .taskPattern("debugging and error analysis")
                .strategy("1. Read the error message carefully, noting the exception type and location\n" +
                        "2. Identify what the code was trying to do when the error occurred\n" +
                        "3. Check the values of relevant variables at that point\n" +
                        "4. Trace backward to find where the problematic value originated\n" +
                        "5. Propose a fix and explain why it resolves the issue")
                .pitfallsToAvoid("Don't just treat the symptom - find the root cause. " +
                        "Avoid suggesting fixes that might break other functionality.")
                .confidenceScore(0.85)
                .build();

        // Strategy for code review
        ReasoningStrategy codeReviewStrategy = ReasoningStrategy.builder()
                .taskPattern("code review and analysis")
                .strategy("1. First, understand what the code is supposed to do\n" +
                        "2. Check for logical errors and edge cases\n" +
                        "3. Look for potential null pointer issues\n" +
                        "4. Review error handling and resource management\n" +
                        "5. Consider performance implications\n" +
                        "6. Suggest improvements with clear explanations")
                .pitfallsToAvoid("Don't nitpick style issues before checking correctness. " +
                        "Focus on bugs that will actually cause problems in production.")
                .confidenceScore(0.88)
                .build();


        // Store the strategies with their embeddings
        // The embedding is created from the taskPattern for similarity matching
        storeStrategy(reasoningBank, embeddingModel, mathStrategy);
        storeStrategy(reasoningBank, embeddingModel, debugStrategy);
        storeStrategy(reasoningBank, embeddingModel, codeReviewStrategy);


        // Create the ReasoningAugmentor that will retrieve and inject relevant strategies
        ReasoningAugmentor reasoningAugmentor = ReasoningAugmentor.builder()
                .reasoningBank(reasoningBank)
                .embeddingModel(embeddingModel)
                .maxStrategies(2)   // Include up to 2 relevant strategies
                .minScore(0.3)      // Only include strategies with similarity >= 0.3
                .build();


        // Optionally, add chat memory for conversation context
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);


        // Build the AI Service with ReasoningAugmentor
        // When a query is received, relevant strategies will be automatically
        // retrieved and injected into the prompt before sending to the LLM.
        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .reasoningAugmentor(reasoningAugmentor)
                .chatMemory(chatMemory)
                .build();
    }

    private static void storeStrategy(InMemoryReasoningBank bank,
                                      EmbeddingModel embeddingModel,
                                      ReasoningStrategy strategy) {
        Embedding embedding = embeddingModel.embed(strategy.taskPattern()).content();
        bank.store(strategy, embedding);
    }
}
