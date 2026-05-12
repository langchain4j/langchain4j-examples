package _8_non_ai_agents;

import _4_parallel_workflow.HrCvReviewer;
import _4_parallel_workflow.ManagerCvReviewer;
import _4_parallel_workflow.TeamMemberCvReviewer;
import _5_conditional_workflow.EmailAssistant;
import _5_conditional_workflow.InterviewOrganizer;
import _5_conditional_workflow.OrganizingTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.model.chat.ChatModel;
import domain.CvReview;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;

public class _8_Non_AI_Agents {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 100);  // control how much you see from the model calls
    }

    /**
     * Here we how to use non-AI agents (plain Java operators) within agentic workflows.
     * Non-AI agents are simply methods, but can be used as any other type of agent.
     * They are perfect for deterministic operations like calculations, data transformations,
     * and aggregations, where you rather have no LLM involvement.
     * The more steps you can outsource to non-AI agents, the faster, correcter and cheaper your workflows will be.
     * Non-AI agents are preferred over tools for workflows where you want to enforce determinism for certain steps.
     * In this case we want the aggregated score of the reviewers to be calculated deterministically, not by an LLM.
     * We also update the application status in the database deterministically based on the aggregated score.
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. Define the ScoreAggregator non-AI agents in this pacckage

        // 2. Build the AI sub-agents for the parallel review step
        HrCvReviewer hrReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("hrReview")
                .build();

        ManagerCvReviewer managerReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("managerReview")
                .build();

        TeamMemberCvReviewer teamReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("teamMemberReview")
                .build();

        // 3. Build the composed parallel agent
        var executor = Executors.newFixedThreadPool(3);  // keep a reference for later closing

        UntypedAgent parallelReviewWorkflow = AgenticServices
                .parallelBuilder()
                .subAgents(hrReviewer, managerReviewer, teamReviewer)
                .executor(executor)
                .build();

        // 4. Build the full workflow incl. non-AI agent
        UntypedAgent collectFeedback = AgenticServices
                .sequenceBuilder()
                .subAgents(
                        parallelReviewWorkflow,
                        new ScoreAggregator(), // no AgenticServices builder needed for non-AI agents. outputKey 'combinedCvReview' is defined in the class
                        new StatusUpdate(), // takes 'combinedCvReview' as input, no output needed
                        AgenticServices.agentAction(agenticScope -> { // another way to add non-AI agents that can operate on the AgenticScope
                            CvReview review = (CvReview) agenticScope.readState("combinedCvReview");
                            agenticScope.writeState("scoreAsPercentage", review.score * 100); // when agents from different systems communicate, output conversion is often needed
                        })
                )
                .outputKey("scoreAsPercentage") // outputKey defined on the non-AI agent annotation in ScoreAggregator.java
                .build();

        // 5. Load input data
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");

        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "hrRequirements", hrRequirements,
                "phoneInterviewNotes", phoneInterviewNotes,
                "jobDescription", jobDescription
        );

        // 6. Invoke the workflow
        double scoreAsPercentage = (double) collectFeedback.invoke(arguments);
        executor.shutdown();

        System.out.println("=== SCORE AS PERCENTAGE ===");
        System.out.println(scoreAsPercentage);
        // as we can see in the logs, the application status has also been updated accordingly

    }
}