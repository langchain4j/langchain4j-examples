import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;

import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.model.mistralai.MistralAiChatModelName.MISTRAL_SMALL_LATEST;

public class MistralAiStreamingChatModelExamples {

    static class Simple_Streaming_Prompt {

        public static void main(String[] args) {

            MistralAiStreamingChatModel model = MistralAiStreamingChatModel.builder()
                    .apiKey(System.getenv("MISTRAL_AI_API_KEY")) // Please use your own Mistral AI API key
                    .modelName(MISTRAL_SMALL_LATEST)
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            String userMessage = "Write a 100-word poem about Java and AI";

            CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

            model.chat(userMessage, new StreamingChatResponseHandler() {

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
