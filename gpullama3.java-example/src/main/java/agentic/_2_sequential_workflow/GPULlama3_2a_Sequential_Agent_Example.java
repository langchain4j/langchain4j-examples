package agentic._2_sequential_workflow;

import _1_basic_agent.CvGenerator;
import _2_sequential_workflow.CvTailor;
import agentic.util.GPULlama3ChatModelProvider;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import util.StringLoader;

import java.io.IOException;
import java.util.Map;

public class GPULlama3_2a_Sequential_Agent_Example {

    public static void main(String[] args) throws IOException {

        // 0. Define GPU or CPU from args
        if (args.length == 0) {
            System.err.println("Usage: java Main <GPU|CPU>");
            System.exit(1);
        }

        boolean useGPU = args[0].equalsIgnoreCase("GPU");

        // 1. Define the model that will power the agent
        final ChatModel CHAT_MODEL = GPULlama3ChatModelProvider.createChatModel(useGPU);

        // 2. Define the two sub-agents in this package:
        //      - CvGenerator.java
        //      - CvTailor.java

        // 3. Create both agents using AgenticServices
        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(CHAT_MODEL)
                .outputKey("masterCv") // if you want to pass this variable from agent 1 to agent 2,
                // then make sure the output key here matches the input variable name
                // specified in the second agent interface agent_interfaces/CvTailor.java
                .build();
        CvTailor cvTailor = AgenticServices
                .agentBuilder(CvTailor.class)
                .chatModel(CHAT_MODEL) // note that it is also possible to use a different model for a different agent
                .outputKey("tailoredCv") // we need to define the key of the output object
                // if we would put "masterCv" here, the original master CV would be overwritten
                // by the second agent. In this case we don't want this, but it's a useful feature.
                .build();

        ////////////////// UNTYPED EXAMPLE //////////////////////

        // 4. Build the sequence
        UntypedAgent tailoredCvGenerator = AgenticServices // use UntypedAgent unless you define the resulting composed agent, see below
                .sequenceBuilder()
                .subAgents(cvGenerator, cvTailor) // this can be as many as you want, order matters
                .outputKey("tailoredCv") // this is the final output of the composed agent
                // note that you can use as output any field that is part of the AgenticScope
                // for example you could output 'masterCv' instead of tailoredCv (even if in this case that makes no sense)
                .build();

        // 4. Load the arguments from text files in resources/documents/
        // - user_life_story.txt
        // - job_description_backend.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");
        String instructions = "Adapt the CV to the job description below." + StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 5. Because we use an untyped agent, we need to pass a map of arguments
        Map<String, Object> arguments = Map.of(
                "lifeStory", lifeStory, // matches the variable name in agent_interfaces/CvGenerator.java
                "instructions", instructions // matches the variable name in agent_interfaces/CvTailor.java
        );

        // 5. Call the composed agent to generate the tailored CV
        long startTime = System.nanoTime();
        String tailoredCv = (String) tailoredCvGenerator.invoke(arguments);
        long endTime = System.nanoTime();

        // 6. and print the generated CV
        System.out.println("=== TAILORED CV UNTYPED ===");
        System.out.println((String) tailoredCv); // you can observe that the CV looks very different
        // when you'd use job_description_fullstack.txt as input

        // In example 2b we'll build the same sequential agent but with typed output,
        // and we'll inspect the AgenticScope

        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println("\n=== Performance ===");
        System.out.println("Inference time: " + durationMs + " ms");
    }

}
