package _1_basic_agent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import model.Cv;

import java.io.IOException;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class _1_Basic_Agent_Example {

    /**
     * This example demonstrates how to implement a basic agent that will
     * turn a user's life story into a clean and complete CV.
     * Note that running this can take a while because the outputted CV
     * will be quite lengthy and the model needs a while.
     */

    // 1. Define the model that will power the agent
    // Here we log requests and responses. This is optional.
    // You will see the logs when setting log level to DEBUG or lower,
    // and you won't see them when setting log level to INFO or higher.
    private static final ChatModel CHAT_MODEL = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName(GPT_4_O_MINI)
            .logRequests(true)
            .logResponses(true)
            .build();

    public static void main(String[] args) throws IOException {

        // 2. Define the agent behavior in agent_interfaces/CvGenerator.java

        // 3. Create the agent using AgenticServices
        CvGenerator cvGenerator = AgenticServices
                .agentBuilder(CvGenerator.class)
                .chatModel(CHAT_MODEL)
                .outputName("masterCv") // we can optionally define the name of the output object
                .build();

        // 4. Load text file from resources/documents/user_life_story.txt
        String lifeStory = new String(
                _1_Basic_Agent_Example.class.getResourceAsStream("/documents/user_life_story.txt").readAllBytes()
        );

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

        System.out.println("=== CV OBJECT ===");
        System.out.println(cvStructured);

    }
}