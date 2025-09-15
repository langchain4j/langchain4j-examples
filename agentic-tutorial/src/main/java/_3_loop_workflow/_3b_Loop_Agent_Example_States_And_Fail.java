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

public class _3b_Loop_Agent_Example_States_And_Fail {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

    /**
     * Here we build the same loop-agent as in 3a, but this time we should see it fail
     * by trying to tailor the CV to a job description that doesn't fit.
     * We will also return the latest score and feedback, on top of the final CV,
     * which will allow us to check if we obtained a good score and if it's worth handing in this CV.
     * We also show a trick to inspect the intermediary states of the review (it gets overwritten in every loop)
     * by storing them in a list each time the exit condition is checked (ie. after every agent invocation).
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. Create all sub-agents (same as before)
        ScoredCvTailor scoredCvTailor = AgenticServices.agentBuilder(ScoredCvTailor.class)
                .chatModel(CHAT_MODEL)
                .outputName("cv") // this will be updated in every iteration, continuously improving the CV
                .build();
        CvReviewer cvReviewer = AgenticServices.agentBuilder(CvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("cvReview") // this gets updated in every iteration with new feedback for the next tailoring
                .build();

        // 2. Build the sequence and store the reviews on each exit condition check
        // It can be important to know whether the exit condition was met or just the max iterations
        // (eg. John may not even want to bother applying for this job).
        // You can change the output variable to also contain the last score and feedback, and check yourself after the loop finished.
        // You can also store the intermediary values in a mutable list to inspect later.
        // The code below does both things at the same time.
        List<CvReview> reviewHistory = new ArrayList<>();

        UntypedAgent reviewedCvGenerator = AgenticServices // use UntypedAgent unless you define the resulting composed agent, see below
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

        // 3. Load the original arguments from text files in resources/documents/
        // - master_cv.txt
        // - job_description_backend.txt
        String masterCv = StringLoader.loadFromResource("/documents/master_cv.txt");
        String fluteJobDescription = "We are looking for a passionate flute teacher to join our music academy.";
        CvReview fluteJobCvReview = new CvReview(0.5, "Adapt the CV to the following job description as good as you can without inventing skills. Stick to the given facts.: " + fluteJobDescription);

        // 4. Because we use an untyped agent, we need to pass a map of arguments
        Map<String, Object> arguments = Map.of(
                "cv", masterCv, // start with the master CV, it will be continuously improved
                "cvReview", fluteJobCvReview,
                "jobDescription", fluteJobDescription
        );

        // 5. Call the composed agent to generate the tailored CV
        Map<String, Object> cvAndReview = (Map<String, Object>) reviewedCvGenerator.invoke(arguments);

        // You can observe the steps in the logs, for example:
        // Round 1 output: "content": "{\n  \"score\": 0.0,\n  \"feedback\": \"This CV is not suitable for the flute teacher position at our music academy...
        // Round 2 output: "content": "{\n  \"score\": 0.3,\n  \"feedback\": \"John's CV demonstrates strong soft skills such as communication, patience, and adaptability, which are important in a teaching role. However, the absence of formal music training or ...
        // Round 3 output: "content": "{\n  \"score\": 0.4,\n  \"feedback\": \"John Doe demonstrates strong soft skills and mentoring experience,...

        System.out.println("=== REVIEWED CV FOR FLUTE TEACHER ===");
        System.out.println(cvAndReview.get("cv")); // the final CV after the loop

        // now you get the finalReview in the output map so you can check
        // if the final score and feedback meet your requirements
        CvReview review = (CvReview) cvAndReview.get("finalReview");
        System.out.println("=== FINAL REVIEW FOR FLUTE TEACHER ===");
        System.out.println("CV" + (review.score >= 0.8 ? " passes" : " does not pass") + " with score=" + review.score);
        System.out.println("Final feedback: " + review.feedback);

        // in reviewHistory you find the full history of reviews
        System.out.println("=== FULL REVIEW HISTORY FOR FLUTE TEACHER ===");
        System.out.println(reviewHistory);

    }
}
