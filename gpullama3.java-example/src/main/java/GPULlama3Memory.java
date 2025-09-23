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

public class GPULlama3Memory {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        GPULlama3APIStreamingChatModel streamingModel = GPULlama3APIStreamingChatModel.builder().baseUrl("http://localhost:8080").build();

        GPULlama3ChatRequestParameters params = GPULlama3ChatRequestParameters.builder().temperature(0.3)  // Lower temperature for more focused responses
                .maxOutputTokens(4000).topP(0.1)  // Added topP for better control
                .seed(12345L).build();

        // Simple chat memory without token estimation
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // Updated system message with clear instructions
        SystemMessage systemMessage = SystemMessage.from("You are a bot assistant named Bot. " +
                "Answer only the specific question being asked. " +
                "Use conversation history for context but don't repeat information unless directly asked again. " +
                "Be concise and focused."
        );
        chatMemory.add(systemMessage);

        // Simulate a focused 3-turn conversation with rich context

        // Turn 1: Basic introduction
        UserMessage msg1 = UserMessage.from("Hi! I'm Orion. What is your name?");

        System.out.println("=== CONVERSATION START ===\n");
        processMessage(streamingModel, chatMemory, msg1, params);

        // Turn 2: Test memory recall
        UserMessage msg2 = UserMessage.from("What is my name?");

        processMessage(streamingModel, chatMemory, msg2, params);

//        // Turn 3: Test memory recall
//        UserMessage msg3 = UserMessage.from("What do you have in conversation history?");

//        processMessage(streamingModel, chatMemory, msg3, params);

        System.out.println("\n=== MEMORY CONTENTS ===");
        System.out.println("Total messages in memory: " + chatMemory.messages().size());
    }

    private static void processMessage(GPULlama3APIStreamingChatModel model, ChatMemory chatMemory,
            UserMessage userMessage, GPULlama3ChatRequestParameters params)
            throws ExecutionException, InterruptedException {

        chatMemory.add(userMessage);

        System.out.println("[User]: " + userMessage.singleText());
        System.out.print("[Bot]: ");

        AiMessage aiMessage = streamChat(model, chatMemory, params);
        chatMemory.add(aiMessage);

        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    private static AiMessage streamChat(GPULlama3APIStreamingChatModel model, ChatMemory chatMemory,
            GPULlama3ChatRequestParameters params)
            throws ExecutionException, InterruptedException {

        ChatRequest streamRequest = ChatRequest.builder()
                .messages(chatMemory.messages())
                .parameters(params)
                .build();

        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        model.chat(streamRequest, new StreamingChatResponseHandler() {

            @Override
            public void onPartialThinking(PartialThinking partialThinking) {
                // Show thinking process in a different color or style
                System.out.print(partialThinking.text());
            }

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                //System.out.flush();
                futureResponse.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("\nError: " + error.getMessage());
                futureResponse.completeExceptionally(error);
            }
        });

        return futureResponse.get().aiMessage();
    }
}
