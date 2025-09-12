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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class _3_Loop_Agent_Example {

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
        UntypedAgent reviewedCvGenerator = AgenticServices // use UntypedAgent unless you define the resulting compound agent, see _2_Sequential_Agent_Example
                .loopBuilder().subAgents(scoredCvTailor, cvReviewer) // this can be as many as you want, order matters
                .outputName("cv") // this is the final output we want to observe (the improved CV)
                .exitCondition(agenticScope -> {
                            CvReview review = (CvReview) agenticScope.readState("cvReview");
                            System.out.println("Checking exit condition with score=" + review.score); // we log intermediary scores
                            return review.score > 0.8;
                            // TODO Mario would be nice to access the loop counter
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

        // 5. Call the compound agent to generate the tailored CV
        String tailoredCv = (String) reviewedCvGenerator.invoke(arguments);

        // 6. and print the generated CV
        System.out.println("=== REVIEWED CV UNTYPED ===");
        System.out.println((String) tailoredCv);
        // this CV probably passes after the first tailoring + review round
        // if you want to see it fail, try with this jobDescription:

        ////////////////// FAILURE SCENARIO AND AGENTICSCOPE OBSERVATION //////////////////////

        // if you want to see it fail, try with this jobDescription:
        String fluteJobDescription = "We are looking for a passionate flute teacher to join our music academy.";
        CvReview fluteJobCvReview = new CvReview(0.5, "Adapt the CV to the following job description as good as you can without inventing skills. Stick to the given facts.: " + fluteJobDescription);

        // reuse the same masterCv and instructions, just swap job description
        Map<String, Object> fluteArgs = Map.of(
                "cv", masterCv, // start with the master CV, it will be continuously improved
                "cvReview", fluteJobCvReview,
                "jobDescription", fluteJobDescription
        );

        String tailoredFluteCv = (String) reviewedCvGenerator.invoke(fluteArgs);
        // You can observe the steps in the logs, for example:
        // Round 1 output: "content": "{\n  \"score\": 0.0,\n  \"feedback\": \"This CV is not suitable for the flute teacher position at our music academy...
        // Round 2 output: "content": "{\n  \"score\": 0.3,\n  \"feedback\": \"John's CV demonstrates strong soft skills such as communication, patience, and adaptability, which are important in a teaching role. However, the absence of formal music training or ...
        // Round 3 output: "content": "{\n  \"score\": 0.4,\n  \"feedback\": \"John Doe demonstrates strong soft skills and mentoring experience,...

        System.out.println("=== REVIEWED CV FOR FLUTE TEACHER ===");
        System.out.println(tailoredFluteCv);

        // If failing to meet the exit condition within the max iterations is
        // important for information for your use case (eg. John may not even want to bother
        // applying for this job), you can change the output variable to also contain the last score and feedback
        // You can also store the intermediary values in a mutable list to inspect later.
        // The code below does both things at the same time.

        List<CvReview> reviewHistory = new ArrayList<>();

        UntypedAgent reviewedCvGeneratorWithExitCheck = AgenticServices // use UntypedAgent unless you define the resulting compound agent, see below
                .loopBuilder().subAgents(scoredCvTailor, cvReviewer) // this can be as many as you want, order matters
                .outputName("cvAndReview") // this is the final output we want to observe
                .output(agenticScope -> {
                    Map<String, Object> cvAndReview = Map.of(
                            "cv", agenticScope.readState("cv"),
                            "finalReview", agenticScope.readState("cvReview")
                    );
                    return cvAndReview;
                })
                .exitCondition(scope -> {
                    CvReview review = (CvReview) scope.readState("cvReview");
                    reviewHistory.add(review); // capture the score+feedback at every agent invocation
                    System.out.println("Exit check with score=" + review.score);
                    return review.score >= 0.8;
                })
                .maxIterations(3) // safety to avoid infinite loops, in case exit condition is never met
                .build();

        // now you get the finalReview in the output map so you can check
        // if the final score and feedback meet your requirements

        Map<String, Object> cvAndReview = (Map<String, Object>) reviewedCvGeneratorWithExitCheck.invoke(fluteArgs);
        System.out.println("=== CV AND FINAL REVIEW FOR FLUTE TEACHER ===");
        System.out.println(cvAndReview);

        // in reviewHistory you find the full history of reviews
        System.out.println("=== FULL REVIEW HISTORY FOR FLUTE TEACHER ===");
        System.out.println(reviewHistory);

    }
}
