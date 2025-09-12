import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.gpullama3.GPULlama3StreamingChatModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class GPULlama3StreamChatModelExamples {

    public static void main(String[] args) {
        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        String prompt;

        if (args.length > 0) {
            prompt = args[0];
            System.out.println("User Prompt: " + prompt);
        } else {
            prompt = "What is the capital of France?";
            System.out.println("Example Prompt: " + prompt);
        }

        // @formatter:off
        ChatRequest request = ChatRequest.builder().messages(
                        UserMessage.from(prompt),
                        SystemMessage.from("reply with extensive sarcasm"))
                .build();

        Path modelPath = Paths.get("beehive-llama-3.2-1b-instruct-fp16.gguf");


        GPULlama3StreamingChatModel model = GPULlama3StreamingChatModel.builder().modelPath(modelPath).build();


        model.chat(request, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                futureResponse.complete(completeResponse);
                model.printLastMetrics();
            }

            @Override
            public void onError(Throwable error) {
                futureResponse.completeExceptionally(error);
            }
        });

        futureResponse.join();
    }
}
