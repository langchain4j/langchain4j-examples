import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.gpullama3.GPULlama3StreamingChatModel;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class GPULlama3StreamingChatModelExample {

    public static void main(String[] args) {
        // Read path to your *local* model files.
        String localLLMsPath = System.getenv("LOCAL_LLMS_PATH");

        // Check if the environment variable is set
        if (localLLMsPath == null || localLLMsPath.isEmpty()) {
            System.err.println("Error: LOCAL_LLMS_PATH environment variable is not set.");
            System.err.println("Please set this environment variable to the directory containing your local model files.");
            System.exit(1);
        }

        // Change this model file name to choose any of your *local* model files.
        // Supports Mistral, Llama3, Phi-3, Qwen2.5 and Qwen3 in gguf format.
        String modelFile = "beehive-llama-3.2-1b-instruct-fp16.gguf";
        Path modelPath = Path.of(localLLMsPath, modelFile);

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

        GPULlama3StreamingChatModel model = GPULlama3StreamingChatModel.builder()
                .onGPU(Boolean.TRUE) // if false, runs on CPU though a lightweight implementation of llama3.java
                .modelPath(modelPath)
                .build();
        // @formatter:on

        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

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
