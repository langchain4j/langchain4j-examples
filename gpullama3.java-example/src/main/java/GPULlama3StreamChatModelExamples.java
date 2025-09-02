import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.gpullama3.GPULlama3StreamingChatModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class GPULlama3StreamChatModelExamples {

    public static void main(String[] args) {
        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        Path modelPath = Paths.get("beehive-llama-3.2-1b-instruct-fp16.gguf");

        StreamingChatModel model = GPULlama3StreamingChatModel.builder().modelPath(modelPath).build();

        model.chat("What is the best part of France and why?", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                futureResponse.complete(completeResponse);
                System.out.println("\n\nDone streaming");
            }

            @Override
            public void onError(Throwable error) {
                futureResponse.completeExceptionally(error);
            }
        });

        futureResponse.join();
    }
}
