import dev.langchain4j.model.gpullama3.GPULlama3ChatModel;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GPULlama3ChatModelExamples {

    public static void main(String[] args) {

        Path modelPath = Paths.get("beehive-llama-3.2-1b-instruct-fp16.gguf");
        GPULlama3ChatModel model = GPULlama3ChatModel.builder().modelPath(modelPath).build();

        String prompt = "What is the capital of France?";
        String res = model.chat(prompt);
        System.out.println(res);

        //Optionally print metrics
        model.printLastMetrics();
    }
}
