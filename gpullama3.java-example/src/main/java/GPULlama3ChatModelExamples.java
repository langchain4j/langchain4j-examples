import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.gpullama3.GPULlama3ChatModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Handler;

public class GPULlama3ChatModelExamples {

    public static void main(String[] args) {

        // Change this path to the path of your model file.
        // Supports Mistral, Llama3, Phi-3, Qwen2.5 and Qwen3 in gguf format.

        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("What is the capital of France?"))
                .build();

        Path modelPath = Paths.get("beehive-llama-3.2-1b-instruct-fp16.gguf");

        GPULlama3ChatModel model = GPULlama3ChatModel
                                    .builder()
                                    .modelPath(modelPath)
                                    .onGPU(Boolean.TRUE)
                                    .build();


        ChatResponse response = model.chat(request);
//        String res = model.chat(request);
        System.out.println(response.aiMessage().text());

        //Optionally print metrics
//        model.printLastMetrics();
    }
}
