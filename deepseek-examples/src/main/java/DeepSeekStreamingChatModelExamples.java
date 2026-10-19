import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.util.concurrent.CompletableFuture;

/**
 * Example of streaming responses from DeepSeek.
 */
public class DeepSeekStreamingChatModelExamples {

    public static void main(String[] args) throws Exception {

        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("DEEPSEEK_API_KEY")) // Please use your own DeepSeek API key
                .baseUrl("https://api.deepseek.com")
                .modelName("deepseek-flash")
                .logRequests(true)
                .logResponses(true)
                .build();

        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        model.chat("Tell me a short story about a mail carrier in 3 sentences", new StreamingChatResponseHandler() {

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

        futureResponse.get();
    }
}
