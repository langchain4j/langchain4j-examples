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
     * This example demonstrates how to use non-AI agents (plain Java operators) within agentic workflows.
     * Non-AI agents are simply methods, but can be used as any other type of agent.
     * They are perfect for deterministic operations like calculations, data transformations,
     * and aggregations, where you rather have no LLM involvement.
     * We show how to use the non-AI ScoreAggregator agent in a composed workflow.
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. Define all normal AI-agents
        HrCvReviewer hrReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("hrReview")
                .build();

        ManagerCvReviewer managerReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("managerReview")
                .build();

        TeamMemberCvReviewer teamReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("teamMemberReview")
                .build();

        // 2. Build a composed parallel agent for step 1
        UntypedAgent parallelReviewWorkflow = AgenticServices
                .parallelBuilder()
                .subAgents(hrReviewer, managerReviewer, teamReviewer)
                .executor(Executors.newFixedThreadPool(3))
                .build();

        // 3. Build the full workflow incl. non-AI agent
        UntypedAgent collectFeedback = AgenticServices
                .sequenceBuilder()
                .subAgents(parallelReviewWorkflow, new ScoreAggregator()) // no AgenticServices builder needed for non-AI agents
                .outputName("combinedCvReview") // outputName defined on the non-AI agent annotation in ScoreAggregator.java
                .build();

        // 4. Load input data
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

        // 5. Invoke the workflow
        CvReview combinedReview = (CvReview) collectFeedback.invoke(arguments);

        System.out.println("=== COMBINED REVIEW ===");
        System.out.println(combinedReview);

    }
}