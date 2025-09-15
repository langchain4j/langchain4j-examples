package _2_sequential_workflow;

import _1_basic_agent.CvGenerator;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.model.chat.ChatModel;
import util.AgenticScopePrinter;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;

public class _2b_Sequential_Agent_Example_Typed {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 150);  // control how much you see from the model calls
    }

    /**
     * We'll implement the same sequential workflow as in 2a, but this time we'll
     * - use a typed interface for the composed agent (SequenceCvGenerator)
     * - which will allow us to use its method with arguments instead of .invoke(argsMap)
     * - collect the output in a custom way
     * - retrieve and inspect the AgenticScope after invocation, for debugging or testing purposes
     */

    // 1. Define the model that will power the agents
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. Define the sequential agent interface in this package:
        //      - SequenceCvGenerator.java
        // with method signature:
        // ResultWithAgenticScope<Map<String, String>> generateTailoredCv(@V("lifeStory") String lifeStory, @V("instructions") String instructions);

        // 3. Create both sub-agents using AgenticServices like before
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


        // 4. Load the arguments from text files in resources/documents/
        // (no need to put them in a Map this time)
        // - user_life_story.txt
        // - job_description_backend.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");
        String instructions = "Adapt the CV to the job description below." + StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 5. Build the typed sequence with custom output handling
        SequenceCvGenerator sequenceCvGenerator = AgenticServices
                .sequenceBuilder(SequenceCvGenerator.class) // here we specify the typed interface
                .subAgents(cvGenerator, cvTailor)
                .outputName("bothCvsAndLifeStory")
                .output(agenticScope -> { // any method is possible, but we collect some internal variables.
                    Map<String, String> bothCvsAndLifeStory = Map.of(
                            "lifeStory", agenticScope.readState("lifeStory", ""),
                            "masterCv", agenticScope.readState("masterCv", ""),
                            "tailoredCv", agenticScope.readState("tailoredCv", "")
                    );
                    return bothCvsAndLifeStory;
                    })
                .build();

        // 6. Call the typed composed agent
        ResultWithAgenticScope<Map<String,String>> bothCvsAndScope = sequenceCvGenerator.generateTailoredCv(lifeStory, instructions);

        // 7. Extract result and agenticScope
        AgenticScope agenticScope = bothCvsAndScope.agenticScope();
        Map<String,String> bothCvsAndLifeStory = bothCvsAndScope.result();

        System.out.println("=== USER INFO (input) ===");
        String userStory = bothCvsAndLifeStory.get("lifeStory");
        System.out.println(userStory.length() > 100 ? userStory.substring(0, 100) + " [truncated...]" : lifeStory);
        System.out.println("=== MASTER CV TYPED (intermediary variable) ===");
        String masterCv = bothCvsAndLifeStory.get("masterCv");
        System.out.println(masterCv.length() > 100 ? masterCv.substring(0, 100) + " [truncated...]" : masterCv);
        System.out.println("=== TAILORED CV TYPED (output) ===");
        String tailoredCv = bothCvsAndLifeStory.get("tailoredCv");
        System.out.println(tailoredCv.length() > 100 ? tailoredCv.substring(0, 100) + " [truncated...]" : tailoredCv);

        // Both untyped and typed agents give the same tailoredCv result
        // (any differences are due to the non-deterministic nature of LLMs),
        // but the typed agent is more elegant to use and safer because of compile-time type checking

        System.out.println("=== INVOCATION TRACE (all messages in the conversation) ===");
        System.out.println(AgenticScopePrinter.printPretty(agenticScope, 100));
        // this will return this object (filled out):
        // AgenticScope {
        //     memoryId = "e705028d-e90e-47df-9709-95953e84878c",
        //             state = {
        //                     bothCvsAndLifeStory = { // output
        //                             masterCv = "...",
        //                            lifeStory = "...",
        //                            tailoredCv = "..."
        //                     },
        //                     instructions = "...", // inputs and intermediary variables
        //                     tailoredCv = "...",
        //                     masterCv = "...",
        //                     lifeStory = "..."
        //             }
        // }
        System.out.println(agenticScope.contextAsConversation()); // will work in next release

    }
}