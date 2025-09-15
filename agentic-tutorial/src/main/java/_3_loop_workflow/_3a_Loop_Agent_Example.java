package _3_loop_workflow;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import domain.CvReview;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;

public class _3a_Loop_Agent_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

    /**
     * This example demonstrates how to implement a CvReviewer agent that we can add to a loop
     * with our CvTailor agent. We will implement two agents:
     * - ScoredCvTailor (takes in a CV and tailors it to a CvReview (feedback/instruction + score))
     * - CvReviewer (takes in the tailored CV and job description, and returns a CvReview object (feedback + score)
     * Additionally, the loop ends when the score is above a certain threshold (e.g. 0.7) (exit condition)
     */

    // 1. Define the model that will power the agents
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. Define the two sub-agents in this package:
        //      - CvTailor.java
        //      - CvReviewer.java

        // 3. Create all agents using AgenticServices
        ScoredCvTailor scoredCvTailor = AgenticServices.agentBuilder(ScoredCvTailor.class)
                .chatModel(CHAT_MODEL)
                .outputName("cv") // this will be updated in every iteration, continuously improving the CV
                .build();
        CvReviewer cvReviewer = AgenticServices.agentBuilder(CvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("cvReview") // this gets updated in every iteration with new feedback for the next tailoring
                .build();

        // 4. Build the sequence
        UntypedAgent reviewedCvGenerator = AgenticServices // use UntypedAgent unless you define the resulting composed agent, see _2_Sequential_Agent_Example
                .loopBuilder().subAgents(scoredCvTailor, cvReviewer) // this can be as many as you want, order matters
                .outputName("cv") // this is the final output we want to observe (the improved CV)
                .exitCondition(agenticScope -> {
                            CvReview review = (CvReview) agenticScope.readState("cvReview");
                            System.out.println("Checking exit condition with score=" + review.score); // we log intermediary scores
                            return review.score > 0.8;
                        }) // exit condition based on the score given by the CvReviewer agent, when > 0.8 we are satisfied
                // note that the exit condition is checked after each agent invocation, not just after the entire loop
                .maxIterations(3) // safety to avoid infinite loops, in case exit condition is never met
                .build();

        // 5. Load the original arguments from text files in resources/documents/
        // - master_cv.txt
        // - job_description_backend.txt
        String masterCv = StringLoader.loadFromResource("/documents/master_cv.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        CvReview cvReview = new CvReview(0.5, "Adapt the CV to the following job description as good as you can without inventing skills. Stick to the given facts.: " + jobDescription);

        // 5. Because we use an untyped agent, we need to pass a map of arguments
        Map<String, Object> arguments = Map.of(
                "cv", masterCv, // start with the master CV, it will be continuously improved
                "cvReview", cvReview,
                "jobDescription", jobDescription
        );

        // 5. Call the composed agent to generate the tailored CV
        String tailoredCv = (String) reviewedCvGenerator.invoke(arguments);

        // 6. and print the generated CV
        System.out.println("=== REVIEWED CV UNTYPED ===");
        System.out.println((String) tailoredCv);

        // this CV probably passes after the first tailoring + review round
        // if you want to see it fail, try with the flute teacher jobDescription
        // as in example 3b, where we also inspect intermediary states of the CV
        // and retrieve the final review and score as well.

    }
}
