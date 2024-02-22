import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.model.output.Response;

import java.util.concurrent.CompletableFuture;

public class MistralAiStreamingChatModelExamples {

    static class Simple_Streaming_Prompt {

        public static void main(String[] args) {

            MistralAiStreamingChatModel model = MistralAiStreamingChatModel.builder()
                    .apiKey(System.getenv("MISTRAL_AI_API_KEY")) // Please use your own Mistral AI API key
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            String userMessage = "Write a 100-word poem about Java and AI";

            CompletableFuture<Response<AiMessage>> futureResponse = new CompletableFuture<>();
            model.generate(userMessage, new StreamingResponseHandler<AiMessage>() {

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
