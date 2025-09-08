import dev.langchain4j.model.gpullama3.GPULlama3ChatModel;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GPULlama3ChatModelExamples {

    public static void main(String[] args) {

        // Change this path to the path of your model file.
        // Supports Mistral, Llama3, Phi-3, Qwen2.5 and Qwen3 in gguf format.
        Path modelPath = Paths.get("beehive-llama-3.2-1b-instruct-fp16.gguf");
        GPULlama3ChatModel model = GPULlama3ChatModel
                                    .builder()
                                    .modelPath(modelPath)
                                    .onGPU(true)
                                    .build();

        String prompt = "What is the capital of France?";
        String res = model.chat(prompt);
        System.out.println(res);

        //Optionally print metrics
//        model.printLastMetrics();
    }
}
