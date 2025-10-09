package agentic.util;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.gpullama3.GPULlama3ChatModel;

import java.nio.file.Path;

/**
 * This class provides an instance of {@link ChatModel} for the {@link GPULlama3ChatModel}.
 */
public class GPULlama3ChatModelProvider {

    public static ChatModel createChatModel(boolean onGPU) {
        return GPULlama3ChatModel.builder()
                .modelPath(getModelPath())
                .maxTokens(1500)
                .onGPU(onGPU) //if false, runs on CPU though a lightweight implementation of llama3.java
                .build();
    }

    private static Path getModelPath() {
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

        return Path.of(localLLMsPath, modelFile);
    }
}
