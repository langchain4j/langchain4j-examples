package _6_composed_workflow;

import _1_basic_agent.CvGenerator;
import _3_loop_workflow.CvReviewer;
import _3_loop_workflow.ScoredCvTailor;
import _4_parallel_workflow.HrCvReviewer;
import _4_parallel_workflow.ManagerCvReviewer;
import _4_parallel_workflow.TeamMemberCvReviewer;
import _5_conditional_workflow.EmailAssistant;
import _5_conditional_workflow.InterviewOrganizer;
import _5_conditional_workflow.OrganizingTools;
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

public class _6_Composed_Workflow_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

    /**
     * Every agent, whether a single-task agent, a sequential workflow,..., is still just an Agent.
     * This makes agents fully composable:
     * - You can bundle smaller agents into super-agents
     * - Or decompose tasks into subagents
     * - And freely mix sequential, parallel, loop, supervisor, ... workflows at any level
     * Every agent, whether a single-task agent, a sequential workflow,..., is still just an Agent.
     * <p>
     * In this example, we’ll take the composed agents we built earlier (Sequential, Parallel, etc.)
     * and combine them into two larger agents that orchestrate the entire application process.
     */

    // 1. Define the model that will power the agents
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {
//
//        ////////////////// CANDIDATE COMPOSED WORKFLOW //////////////////////
//        // We'll go from life story > CV > Review > review loop until we pass
//        // then email our CV to the company
//
//        // 1. Create all necessary agents for candidate workflow
//        CvGenerator cvGenerator = AgenticServices
//                .agentBuilder(CvGenerator.class)
//                .chatModel(CHAT_MODEL)
//                .outputName("cv")
//                .build();
//
//        ScoredCvTailor scoredCvTailor = AgenticServices
//                .agentBuilder(ScoredCvTailor.class)
//                .chatModel(CHAT_MODEL)
//                .outputName("cv")
//                .build();
//
//        CvReviewer cvReviewer = AgenticServices
//                .agentBuilder(CvReviewer.class)
//                .chatModel(CHAT_MODEL)
//                .outputName("cvReview")
//                .build();
//
//        // 2. Create the loop workflow for CV improvement
//        UntypedAgent cvImprovementLoop = AgenticServices
//                .loopBuilder()
//                .subAgents(scoredCvTailor, cvReviewer)
//                .outputName("cv")
//                .exitCondition(agenticScope -> {
//                    CvReview review = (CvReview) agenticScope.readState("cvReview");
//                    System.out.println("CV Review Score: " + review.score);
//                    if (review.score >= 0.8)
//                        System.out.println("CV is good enough, exiting loop.\n");
//                    return review.score >= 0.8;
//                })
//                .maxIterations(3)
//                .build();
//
//        // 3. Create the complete candidate workflow: Generate > Review > Improve Loop
//        CandidateWorkflow candidateWorkflow = AgenticServices
//                .sequenceBuilder(CandidateWorkflow.class)
//                .subAgents(cvGenerator, cvReviewer, cvImprovementLoop)
//                // here we use the composed agent cvImprovementLoop inside the sequenceBuilder
//                // we also need the cvReviewer in order to generate a first review before entering the loop
//                .outputName("cv")
//                .build();
//
//        // 4. Load input data
//        String lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt");
//        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
//
//        // 5. Execute the candidate workflow
//        String candidateResult = candidateWorkflow.processCandidate(lifeStory, jobDescription);
//        // Note that input parameters and intermediate parameters are all stored in one AgenticScope
//        // that is available to all agents in the system
//        // TODO Mario doublecheck that this is true and there's no lower and higher level AgenticScopes at play
//
//        System.out.println("=== CANDIDATE WORKFLOW COMPLETED ===");
//        System.out.println("Final CV: " + candidateResult);
//
//        System.out.println("\n\n\n\n");

        ////////////////// HIRING TEAM COMPOSED WORKFLOW //////////////////////
        // We receive an email with the candidate CV and contacts. We did the phone HR interview.
        // We now go through the 3 parallel reviews then send that result into the conditional flow to invite or reject.

        // 1. Create all necessary agents for hiring team workflow
        HrCvReviewer hrCvReviewer = AgenticServices
                .agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("hrReview")
                .build();

        ManagerCvReviewer managerCvReviewer = AgenticServices
                .agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("managerReview")
                .build();

        TeamMemberCvReviewer teamMemberCvReviewer = AgenticServices
                .agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("teamMemberReview")
                .build();

        EmailAssistant emailAssistant = AgenticServices
                .agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .build();

        InterviewOrganizer interviewOrganizer = AgenticServices
                .agentBuilder(InterviewOrganizer.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .build();

        // 2. Create parallel review workflow
        UntypedAgent parallelReviewWorkflow = AgenticServices
                .parallelBuilder()
                .subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer)
                .executor(Executors.newFixedThreadPool(3))
                .outputName("combinedCvReview")
                .output(agenticScope -> {
                    CvReview hrReview = (CvReview) agenticScope.readState("hrReview");
                    CvReview managerReview = (CvReview) agenticScope.readState("managerReview");
                    CvReview teamMemberReview = (CvReview) agenticScope.readState("teamMemberReview");
                    String feedback = String.join("\n",
                            "HR Review: " + hrReview.feedback,
                            "Manager Review: " + managerReview.feedback,
                            "Team Member Review: " + teamMemberReview.feedback
                    );
                    double avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0;
                    System.out.println("Final averaged CV Review Score: " + avgScore + "\n");
                    return new CvReview(avgScore, feedback);
                })
                .build();

        // 3. Create conditional workflow for final decision
        UntypedAgent decisionWorkflow = AgenticServices
                .conditionalBuilder()
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("combinedCvReview")).score >= 0.8, interviewOrganizer)
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("combinedCvReview")).score < 0.8, emailAssistant)
                .build();

        // 4. Create complete hiring team workflow: Parallel Review → Decision
        HiringTeamWorkflow hiringTeamWorkflow = AgenticServices
                .sequenceBuilder(HiringTeamWorkflow.class)
                .subAgents(parallelReviewWorkflow, decisionWorkflow)
                .build();

        // 5. Load input data
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        
        // Put all data in a Map for easy access
        Map<String, Object> inputData = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "hrRequirements", hrRequirements,
                "phoneInterviewNotes", phoneInterviewNotes,
                "jobDescription", jobDescription
        );

        // 6. Execute the hiring team workflow
        hiringTeamWorkflow.processApplication(candidateCv, jobDescription, hrRequirements, phoneInterviewNotes, candidateContact);

        System.out.println("=== HIRING TEAM WORKFLOW COMPLETED ===");
        System.out.println("Parallel reviews completed and decision made");

    }
}