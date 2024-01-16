import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.model.output.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;

public class _05_Memory {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        Tokenizer tokenizer = new OpenAiTokenizer(GPT_3_5_TURBO);
        ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(1000, tokenizer);

        UserMessage userMessage1 = userMessage(
                "How do I optimize database queries for a large-scale e-commerce platform? "
                        + "Answer short in three to five lines maximum.");
        chatMemory.add(userMessage1);

        System.out.println("[User]: " + userMessage1.text());
        System.out.print("[LLM]: ");

        CompletableFuture<AiMessage> futureAiMessage = new CompletableFuture<>();

        StreamingResponseHandler<AiMessage> handler = new StreamingResponseHandler<AiMessage>() {

            @Override
            public void onNext(String token) {
                System.out.print(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                futureAiMessage.complete(response.content());
            }

            @Override
            public void onError(Throwable throwable) {
            }
        };

        model.generate(chatMemory.messages(), handler);
        chatMemory.add(futureAiMessage.get());

        UserMessage userMessage2 = userMessage(
                "Give a concrete example implementation of the first point? " +
                        "Be short, 10 lines of code maximum.");
        chatMemory.add(userMessage2);

        System.out.println("\n\n[User]: " + userMessage2.text());
        System.out.print("[LLM]: ");

        model.generate(chatMemory.messages(), handler);
    }
}

//SystemMessage systemMessage = SystemMessage.from(
//"You are a senior developer explaining to another senior developer, "
//      + "the project you are working on is an e-commerce platform with Java back-end, " +
//      "Oracle database,and Spring Data JPA");
//chatMemory.add(systemMessage);
