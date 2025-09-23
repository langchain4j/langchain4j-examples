import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.gpullama3.api.GPULlama3APIStreamingChatModel;
import dev.langchain4j.model.gpullama3.api.GPULlama3ChatRequestParameters;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GPULlama3Conversation {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Create model parameters
        GPULlama3ChatRequestParameters params = GPULlama3ChatRequestParameters.builder()
                .temperature(0.1)
                .maxOutputTokens(4000)
                .topP(0.1)
                .seed(12345L)
                .build();

        // Create models with parameters
        GPULlama3APIStreamingChatModel model = GPULlama3APIStreamingChatModel.builder()
                .baseUrl("http://localhost:8080")
                .build();

        // Create separate chat memories for each agent
        ChatMemory agent1Memory = MessageWindowChatMemory.withMaxMessages(20);
        ChatMemory agent2Memory = MessageWindowChatMemory.withMaxMessages(20);

        // Add system messages to memories
        agent1Memory.add(
                SystemMessage.from("Your name is Bot1. "
                        + "You should collaborate for problem solving by either asking a one question or answering. "
                        + "In every response state who you are. "
                        + "Be short and concise."));
        //+ "Be strategic in sharing information and asking the right questions."));

        agent2Memory.add(
                SystemMessage.from("Your name is Bot2."
                        + "Your user is Bot1."
                        + "You hold number 4."
                        + "You should collaborate for problem solving by either asking a one question or answering."
                        + "In every response state who you are. "
                        + "Be short and concise."));

        String initialMessage = "You hold a number which is 1. Collaborate with Bot2 for a task. The task is to ask Bot2 for its number and then add it with yours and provide the result.";

        // shared objects
        //StreamingChatResponseHandler handler = createStreamingHandler("Agent", futureResponse);
        ChatRequest request;
        ChatResponse response;
        String currentMessage;

        ////// turn 1: bot1
        System.out.print("\nAgent1: ");
        CompletableFuture<ChatResponse> futureResponse1 = new CompletableFuture<>();
        // step1: update memory
        agent1Memory.add(UserMessage.from(initialMessage));
        // step2: request
        request = ChatRequest.builder()
                .messages(agent1Memory.messages())
                .parameters(params)
                .build();
        model.chat(request, createStreamingHandler(futureResponse1));
        // step3:
        response = futureResponse1.get();
        currentMessage = response.aiMessage().text();
        agent1Memory.add(AiMessage.from(currentMessage));

        ////// turn2: bot 2
        System.out.print("\nAgent2: ");
        CompletableFuture<ChatResponse> futureResponse2 = new CompletableFuture<>();
        agent2Memory.add(UserMessage.from(currentMessage));
        request = ChatRequest.builder()
                .messages(agent2Memory.messages())
                .parameters(params)
                .build();
        model.chat(request, createStreamingHandler(futureResponse2));
        response = futureResponse2.get();
        currentMessage = response.aiMessage().text();
        agent2Memory.add(AiMessage.from(currentMessage));

        ////// turn 3: bot1
        System.out.print("\nAgent1: ");
        CompletableFuture<ChatResponse> futureResponse3 = new CompletableFuture<>();
        agent1Memory.add(UserMessage.from(currentMessage));
        request = ChatRequest.builder()
                .messages(agent1Memory.messages())
                .parameters(params)
                .build();
        model.chat(request, createStreamingHandler(futureResponse3));
        response = futureResponse3.get();
        currentMessage = response.aiMessage().text();
        agent1Memory.add(AiMessage.from(currentMessage));

        ////// turn4: bot 2
        System.out.print("\nAgent2: ");
        CompletableFuture<ChatResponse> futureResponse4 = new CompletableFuture<>();
        agent2Memory.add(UserMessage.from(currentMessage));
        request = ChatRequest.builder()
                .messages(agent2Memory.messages())
                .parameters(params)
                .build();
        model.chat(request, createStreamingHandler(futureResponse4));
        response = futureResponse4.get();
        currentMessage = response.aiMessage().text();
        agent2Memory.add(AiMessage.from(currentMessage));
    }

    private static StreamingChatResponseHandler createStreamingHandler(CompletableFuture<ChatResponse> future) {
        return new StreamingChatResponseHandler() {
            private final StringBuilder responseBuilder = new StringBuilder();

            @Override
            public void onPartialThinking(PartialThinking partialThinking) {
                // Optionally show thinking process
                // System.out.print("[" + agentName + " thinking...]");
                System.out.print(partialThinking.text());
            }

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
                responseBuilder.append(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                System.out.flush(); // Ensure all output is printed
                future.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("\n[ Error]: " + error.getMessage());
                future.completeExceptionally(error);
            }
        };
    }
}
