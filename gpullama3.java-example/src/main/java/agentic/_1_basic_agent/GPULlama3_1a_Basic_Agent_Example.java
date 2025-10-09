package agentic._1_basic_agent;

import _1_basic_agent.CvGenerator;
import agentic.util.GPULlama3ChatModelProvider;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import util.StringLoader;

import java.io.IOException;

/**
 * Demonstrates how to deploy GPULlama3 as the model provider for
 * the _1a_Basic_Agent_Example from the agentic-tutorial.
 *
 * This adaptation of _1a_Basic_Agent_Example replaces the standard ChatModelProvider
 * with GPULlama3ChatModelProvider, enabling local GPU-accelerated or CPU-based inference
 * for the CV generation agent. The example maintains the same agent behavior and workflow
 * while showcasing how to integrate a local LLM model instead of cloud-based providers.
 *
 * Key differences from the original tutorial example:
 * - Uses GPULlama3ChatModelProvider instead of ChatModelProvider
 * - Requires command-line argument (GPU/CPU) to select hardware acceleration
 * - Includes performance benchmarking to measure local inference time
 * - Requires LOCAL_LLMS_PATH environment variable for model file location
 *
 * Usage: Run with "GPU" or "CPU" as a command-line argument.
 */

public class GPULlama3_1a_Basic_Agent_Example {

    public static void main(String[] args) throws IOException {

        // 0. Define GPU or CPU from args
        if (args.length == 0) {
            System.err.println("Usage: java Main <GPU|CPU>");
            System.exit(1);
        }

        boolean useGPU = args[0].equalsIgnoreCase("GPU");

        // 1. Define the model that will power the agent
        final ChatModel CHAT_MODEL = GPULlama3ChatModelProvider.createChatModel(useGPU);

        // 2. Define the agent behavior in agent_interfaces/CvGenerator.java

        // 3. Create the agent using AgenticServices
        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(CHAT_MODEL)
                .outputName("masterCv") // we can optionally define the name of the output object
                .build();

        // 4. Load text file from resources/documents/user_life_story.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");

        // 5. We call the agent to generate the CV
        long startTime = System.nanoTime();
        String cv = cvGenerator.generateCv(lifeStory);
        long endTime = System.nanoTime();


        // 6. and print the generated CV
        System.out.println("=== CV ===");
        System.out.println(cv);

        // In example 1b we'll build the same agent but with structured output

        long durationMs = (endTime - startTime) / 1_000_000; // convert to ms
        System.out.println("\n=== Performance ===");
        System.out.println("Inference time: " + durationMs + " ms");

    }
}
