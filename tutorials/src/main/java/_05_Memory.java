import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class _05_Memory {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .modelName(GPT_4_O_MINI)
                .build();

        ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(1000, new OpenAiTokenizer());

        SystemMessage systemMessage = SystemMessage.from(
                "You are a senior developer explaining to another senior developer, "
                        + "the project you are working on is an e-commerce platform with Java back-end, " +
                        "Oracle database,and Spring Data JPA");
        chatMemory.add(systemMessage);


        UserMessage userMessage1 = userMessage(
                "How do I optimize database queries for a large-scale e-commerce platform? "
                        + "Answer short in three to five lines maximum.");
        chatMemory.add(userMessage1);

        System.out.println("[User]: " + userMessage1.text());
        System.out.print("[LLM]: ");

        CompletableFuture<AiMessage> futureAiMessage1 = new CompletableFuture<>();

        StreamingChatResponseHandler handler1 = new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                futureAiMessage1.complete(completeResponse.aiMessage());
            }

            @Override
            public void onError(Throwable throwable) {
            }
        };

        model.chat(chatMemory.messages(), handler1);
        chatMemory.add(futureAiMessage1.get());

        UserMessage userMessage2 = userMessage(
                "Give a concrete example implementation of the first point? " +
                        "Be short, 10 lines of code maximum.");
        chatMemory.add(userMessage2);

        System.out.println("\n\n[User]: " + userMessage2.text());
        System.out.print("[LLM]: ");
        
        CompletableFuture<AiMessage> futureAiMessage2 = new CompletableFuture<>();

        StreamingChatResponseHandler handler2 = new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                futureAiMessage2.complete(completeResponse.aiMessage());
            }

            @Override
            public void onError(Throwable throwable) {
            }
        };
        
        model.chat(chatMemory.messages(), handler2);
        chatMemory.add(futureAiMessage2.get());
    }
}
