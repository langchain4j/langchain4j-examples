package _1_basic_agent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import domain.Cv;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;

public class _1b_Basic_Agent_Example_Structured {
    /**
     * This example implements the same CvGenerator agent as in 1a,
     * but this version will return a custom Java objects, Cv, as defined in model/Cv.java
     */

    // Set logging level
    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

    // 1. Define the model that will power the agent
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. Define the agent behavior in agent_interfaces/CvGeneratorStructuredOutput.java

        // 3. Create the agent using AgenticServices
        CvGeneratorStructuredOutput cvGeneratorStructuredOutput = AgenticServices
                .agentBuilder(CvGeneratorStructuredOutput.class)
                .chatModel(CHAT_MODEL)
                .build();

        // 4. Load text file from resources/documents/user_life_story.txt
        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");

        // 5. Retrieve a Cv object from the agent
        Cv cvStructured = cvGeneratorStructuredOutput.generateCv(lifeStory);

        System.out.println("\n\n=== CV OBJECT ===");
        System.out.println(cvStructured);
    }
}