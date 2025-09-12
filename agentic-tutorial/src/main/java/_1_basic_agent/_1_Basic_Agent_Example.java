package _1_basic_agent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import domain.Cv;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;

public class _1_Basic_Agent_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

    /**
     * This example demonstrates how to implement a basic agent to demonstrate the syntax
     * Note that agents are only useful when combined with other agents, which we will show in the next steps.
     * For just one agent, you can better use an AiService.
     *
     * This basic agent turns a user's life story into a clean and complete CV.
     * Note that running this can take a while because the outputted CV
     * will be quite lengthy and the model needs a while.
     */

    // 1. Define the model that will power the agent
    // The logging behavior is controlled by the flags above
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

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
        String cv = cvGenerator.generateCv(lifeStory);

        // 6. and print the generated CV
        System.out.println("=== CV ===");
        System.out.println(cv);


        ////////////////// STRUCTURED OUTPUT EXAMPLE //////////////////////

        // Agents can also return custom Java objects, as illustrated below
        // This agent will return a Cv object as defined in model/Cv.java

        // Create the agent
        CvGeneratorStructuredOutput cvGeneratorStructuredOutput = AgenticServices
                .agentBuilder(CvGeneratorStructuredOutput.class)
                .chatModel(CHAT_MODEL)
                .build();

        // 5. Retrieve a Cv object from the agent
        Cv cvStructured = cvGeneratorStructuredOutput.generateCv(lifeStory);

        System.out.println("\n\n=== CV OBJECT ===");
        System.out.println(cvStructured);

    }
}