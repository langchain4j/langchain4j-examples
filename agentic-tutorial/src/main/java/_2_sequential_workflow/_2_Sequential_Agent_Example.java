package _2_sequential_workflow;

import _1_basic_agent.CvGenerator;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;

public class _2_Sequential_Agent_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

    /**
     * This example demonstrates how to implement two agents:
     * - CvGenerator (takes in a life story and generates a complete master CV)
     * - CvTailor (takes in the master CV and tailors it to specific instructions (job description, feedback, ...)
     * Then we will call them one after in a fixed workflow
     * using the sequenceBuilder, and demonstrate how to pass a parameter between them.
     * When combining multiple agents, all intermediary parameters and the call chain are
     * stored in the AgenticScope, which is accessible for advanced use cases.
     */

    // 1. Define the model that will power the agents
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. Define the two sub-agents in this package:
        //      - CvGenerator.java
        //      - CvTailor.java

        // 3. Create both agents using AgenticServices
        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(CHAT_MODEL)
                .outputName("masterCv") // if you want to pass this variable from agent 1 to agent 2,
                // then make sure the output name here matches the input variable name
                // specified in the second agent interface agent_interfaces/CvTailor.java
                .build();
        CvTailor cvTailor = AgenticServices
                .agentBuilder(CvTailor.class)
                .chatModel(CHAT_MODEL) // note that it is also possible to use a different model for a different agent
                .outputName("tailoredCv") // we need to define the name of the output object
                // if we would put "masterCv" here, the original master CV would be overwritten
                // by the second agent. In this case we don't want this, but it's a useful feature.
                .build();
        // TODO consider cases where no variable is passed but tools execute other things

        ////////////////// UNTYPED EXAMPLE //////////////////////

        // 4. Build the sequence
        UntypedAgent tailoredCvGenerator = AgenticServices // use UntypedAgent unless you define the resulting compound agent, see below
                .sequenceBuilder()
                .subAgents(cvGenerator, cvTailor) // this can be as many as you want, order matters
                .outputName("tailoredCv") // this is the final output of the compound agent
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

        // 5. Call the compound agent to generate the tailored CV
        String tailoredCv = (String) tailoredCvGenerator.invoke(arguments);

        // 6. and print the generated CV
        System.out.println("=== TAILORED CV UNTYPED ===");
        System.out.println((String) tailoredCv); // you can observe that the CV looks very different
        // when you'd use job_description_fullstack.txt as input

        ////////////////// TYPED EXAMPLE WITH MULTIPLE OUTPUTS AND INVOCATION TRACE //////////////////////

        // Note that the untyped compound agent uses the generic 'invoke' method and
        // both input and output include Objects to support generic use cases.
        // If you know the input and output types, you can define a typed compound agent interface.
        // Here is the same example, but now we use typed agent agent_interfaces/SequenceCvGenerator.java

        // Another thing we'll illustrate here is how to output multiple variables from the AgenticScope
        // including input variables, intermediary variables and output variables.

        SequenceCvGenerator sequenceCvGenerator = AgenticServices
                .sequenceBuilder(SequenceCvGenerator.class) // here we specify the typed interface
                .subAgents(cvGenerator, cvTailor)
                .outputName("bothCvs")
                .output(agenticScope -> {
                    Map<String, String> bothCvs = Map.of(
                            "lifeStory", agenticScope.readState("lifeStory", ""),
                            "masterCv", agenticScope.readState("masterCv", ""),
                            "tailoredCv", agenticScope.readState("tailoredCv", ""),
                            "invocationTrace", agenticScope.contextAsConversation("tailorCv")
                    );
                    return bothCvs;
                    })
                .build();

        Map<String,String> bothCvs = sequenceCvGenerator.generateTailoredCv(lifeStory, instructions);
        System.out.println("=== USER INFO (input) ===");
        System.out.println(bothCvs.get("lifeStory"));
        System.out.println("=== MASTER CV TYPED (intermediary variable) ===");
        System.out.println(bothCvs.get("masterCv"));
        System.out.println("=== TAILORED CV TYPED (output) ===");
        System.out.println(bothCvs.get("tailoredCv"));
        System.out.println("=== INVOCATION TRACE (all messages in the conversation) ===");
        System.out.println(bothCvs.get("invocationTrace"));

        // Both untyped and typed agents give the same result (differences are due to the non-deterministic nature of LLMs)
        // but the typed agent is easier to use and safer because of compile-time type checking

    }
}