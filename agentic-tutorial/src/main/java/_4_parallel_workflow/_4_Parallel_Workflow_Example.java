package _4_parallel_workflow;

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
import java.util.concurrent.Executors;

public class _4_Parallel_Workflow_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

     /**
     * This example demonstrates how to implement 3 parallel CvReviewer agents that will
     * evaluate the CV simultaneously. We will implement three agents:
     * - ManagerCvReviewer (judges how well the candidate will likely do the job)
     *      input: CV and job description
     * - TeamMemberCvReviewer (judges how well the candidate will fit in the team)
     *      input: CV
     * - HrCvReviewer (checks if the candidate qualifies from HR point of view)
     *      input: CV, HR requirements
     */

    // 1. Define the model that will power the agents
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. Define the three sub-agents in this package:
        //      - HrCvReviewer.java
        //      - ManagerCvReviewer.java
        //      - TeamMemberCvReviewer.java

        // 3. Create all agents using AgenticServices
        HrCvReviewer hrCvReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("hrReview") // this will be overwritten in every iteration, and also be used as the final output we want to observe
                .build();

        ManagerCvReviewer managerCvReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("managerReview") // this overwrites the original input instructions, and is overwritten in every iteration and used as new instructions for the CvTailor
                .build();

        TeamMemberCvReviewer teamMemberCvReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("teamMemberReview") // this overwrites the original input instructions, and is overwritten in every iteration and used as new instructions for the CvTailor
                .build();

        // 4. Build the sequence
        UntypedAgent cvReviewGenerator = AgenticServices // use UntypedAgent unless you define the resulting compound agent, see _2_Sequential_Agent_Example
                .parallelBuilder()
                .subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer) // this can be as many as you want
                .executor(Executors.newFixedThreadPool(3)) // optional, by default an internal cached thread pool is used
                .outputName("fullCvReview") // this is the final output we want to observe
                .output(agenticScope -> {
                    // read the outputs of each reviewer from the agentic scope
                    CvReview hrReview = (CvReview) agenticScope.readState("hrReview");
                    CvReview managerReview = (CvReview) agenticScope.readState("managerReview");
                    CvReview teamMemberReview = (CvReview) agenticScope.readState("teamMemberReview");
                    // return a bundled review with averaged score (or any other aggregation you want here)
                    String feedback = String.join("\n",
                            "HR Review: " + hrReview.feedback,
                            "Manager Review: " + managerReview.feedback,
                            "Team Member Review: " + teamMemberReview.feedback
                    );
                    double avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0;

                    return new CvReview(avgScore, feedback);
                        })
                .build();

        // 5. Load the original arguments from text files in resources/documents/
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");

        // 5. Because we use an untyped agent, we need to pass a map of arguments
        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "jobDescription", jobDescription
                ,"hrRequirements", hrRequirements
                ,"phoneInterviewNotes", phoneInterviewNotes
        );

        // 5. Call the compound agent to generate the tailored CV
        var review = cvReviewGenerator.invoke(arguments);

        // 6. and print the generated CV
        System.out.println("=== REVIEWED CV ===");
        System.out.println(review);


        ////////////////// CALLING THE SAME AGENT IN PARALLEL //////////////////////

        // the former example can be rewritten by using the same agent (GenericReviewer)
        // 3 times in parallel, with different inputs (content to review, review instructions)
        // TODO Mario: any way to make this work?

        GenericCvReviewer genericCvReviewer = AgenticServices.agentBuilder(GenericCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("review")
                .build();

        UntypedAgent cvReviewer = AgenticServices.parallelBuilder()
                .subAgents(genericCvReviewer, genericCvReviewer, genericCvReviewer) // same agent 3 times
                .outputName("fullCvReview") // this is the final output we want to observe
                .output(agenticScope -> {
                    // read the outputs of each reviewer from the agentic scope
                    CvReview hrReview = (CvReview) agenticScope.readState("GenericCvReviewer_1");
                    CvReview managerReview = (CvReview) agenticScope.readState("GenericCvReviewer_2");
                    CvReview teamMemberReview = (CvReview) agenticScope.readState("GenericCvReviewer_3");
                    // return a bundled review with averaged score (or any other aggregation you want here)
                    String feedback = String.join("\n",
                            "HR Review: " + hrReview.feedback,
                            "Manager Review: " + managerReview.feedback,
                            "Team Member Review: " + teamMemberReview.feedback
                    );
                    double avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0;

                    return new CvReview(avgScore, feedback);
                        })
                .build();

        // TODO if it works, make different instruction sets and use as systemMessage

        // TODO Mario: this process never finishes, contrary to sequential and co. Should one close the executor / thread pool in some way?


    }
}