import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.jlama.JlamaStreamingChatModel;
import dev.langchain4j.model.output.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JlamaStreamingChatModelExamples {

    static class Simple_Streaming_Prompt {

        public static void main(String[] args) {
            CompletableFuture<Response<AiMessage>> futureResponse = new CompletableFuture<>();

            StreamingChatLanguageModel model = JlamaStreamingChatModel.builder()
                    .modelName("tjake/Llama-3.2-1B-Instruct-JQ4")
                    .temperature(0.3f)
                    .build();

            List<ChatMessage> messages = List.of(
                    SystemMessage.from("You are a helpful chatbot that answers questions in under 30 words."),
                    UserMessage.from("What is the best part of France and why?"));

            model.generate(messages, new StreamingResponseHandler<>() {
                @Override
                public void onNext(String token) {
                    System.out.print(token);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    futureResponse.complete(response);
                }

                @Override
                public void onError(Throwable error) {
                    futureResponse.completeExceptionally(error);
                }
            });

            futureResponse.join();
        }
    }
}
