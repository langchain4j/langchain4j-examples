import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.gpullama3.GPULlama3ChatModel;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GPULlama3ChatModelExamples {

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



        System.out.println("Path: " + modelPath);

        // @formatter:off
        ChatRequest request = ChatRequest.builder().messages(
                UserMessage.from(prompt),
                SystemMessage.from("reply with extensive sarcasm"))
                .build();

        //Path modelPath = Paths.get("beehive-llama-3.2-1b-instruct-fp16.gguf");


        GPULlama3ChatModel model = GPULlama3ChatModel.builder()
                .modelPath(modelPath)
                .onGPU(Boolean.TRUE) //if false, runs on CPU though a lightweight implementation of llama3.java
                .build();
        // @formatter:on

        ChatResponse response = model.chat(request);
        System.out.println("\n" + response.aiMessage().text());

        //Optionally print metrics
        model.printLastMetrics();
    }
}
