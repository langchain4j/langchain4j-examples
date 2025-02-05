import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.jlama.JlamaStreamingChatModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JlamaStreamingChatModelExamples {

    static class Simple_Streaming_Prompt {

        public static void main(String[] args) {
            CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

            StreamingChatLanguageModel model = JlamaStreamingChatModel.builder()
                    .modelName("tjake/Llama-3.2-1B-Instruct-JQ4")
                    .temperature(0.3f)
                    .build();

            List<ChatMessage> messages = List.of(
                    SystemMessage.from("You are a helpful chatbot that answers questions in under 30 words."),
                    UserMessage.from("What is the best part of France and why?"));

            model.chat(messages, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String partialResponse) {
                    System.out.print(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    futureResponse.complete(completeResponse);
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
