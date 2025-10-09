package agentic._1_basic_agent;

import _1_basic_agent.CvGeneratorStructuredOutput;
import agentic.util.GPULlama3ChatModelProvider;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import domain.Cv;
import util.StringLoader;

import java.io.IOException;

/**
 * Demonstrates how to deploy GPULlama3 as the model provider for
 * the _1b_Basic_Agent_Example_Structured from the agentic-tutorial.
 *
 * This adaptation of _1b_Basic_Agent_Example_Structured replaces the standard ChatModelProvider
 * with GPULlama3ChatModelProvider, enabling local GPU-accelerated or CPU-based inference
 * for generating structured CV output. Unlike example 1a which returns plain text, this example
 * produces a structured Cv Java object, demonstrating GPULlama3's capability to generate
 * structured data with local LLM inference.
 *
 * Key differences from the original tutorial example:
 * - Uses GPULlama3ChatModelProvider instead of ChatModelProvider
 * - Requires command-line argument (GPU/CPU) to select hardware acceleration
 * - Includes exception handling for incompatible LLM-generated output
 * - Includes performance benchmarking to measure local inference time
 * - Requires LOCAL_LLMS_PATH environment variable for model file location
 *
 * Usage: Run with "GPU" or "CPU" as a command-line argument.
 */
public class GPULlama3_1b_Basic_Agent_Example_Structured {

    public static void main(String[] args) throws IOException {

        // 0. Define GPU or CPU from args
        if (args.length == 0) {
            System.err.println("Usage: java Main <GPU|CPU>");
            System.exit(1);
        }

        boolean useGPU = args[0].equalsIgnoreCase("GPU");

        // 1. Define the model that will power the agent
        final ChatModel CHAT_MODEL = GPULlama3ChatModelProvider.createChatModel(useGPU);

        // 2. Define the agent behavior in agent_interfaces/CvGeneratorStructuredOutput.java

        // 3. Create the agent using AgenticServices
        CvGeneratorStructuredOutput cvGeneratorStructuredOutput = AgenticServices
                .agentBuilder(CvGeneratorStructuredOutput.class)
                .chatModel(CHAT_MODEL)
                .build();

        // 4. Load text file from resources/documents/user_life_story.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");

        // 5. Retrieve a Cv object from the agent
        long startTime = System.nanoTime();
        long endTime;
        try {
            Cv cvStructured = cvGeneratorStructuredOutput.generateCv(lifeStory);
            endTime = System.nanoTime();

            System.out.println("\n\n=== CV OBJECT ===");
            System.out.println(cvStructured);

        } catch (Exception e) {
            endTime = System.nanoTime(); // still measure inference time
            e.printStackTrace();
            System.out.println("\n\n=== CV OBJECT ===");
            System.out.println("Incompatible LLM-generated output. Cv object not created.");
        }

        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println("\n=== Performance ===");
        System.out.println("Inference time: " + durationMs + " ms");
    }

}
