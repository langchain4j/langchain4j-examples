package _5_conditional_workflow;

import _4_parallel_workflow.ManagerCvReviewer;
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

public class _5b_Conditional_Workflow_Example_Async {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 150);
    }

    /**
     * This example demonstrates multiple fulfilled conditions and async agents that will
     * allow consecutive agents to be called in parallel for faster execution.
     * In this example:
     * - condition 1: if the HrReview is good, the CV is passed to the manager for review,
     * - condition 2: if the HrReview indicates missing information, the candidate is contacted for more info.
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. Create all async agents
        ManagerCvReviewer managerCvReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .async(true) // async agent
                .outputKey("managerReview")
                .build();
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .async(true)
                .tools(new OrganizingTools())
                .outputKey("sentEmailId")
                .build();
        InfoRequester infoRequester = AgenticServices.agentBuilder(InfoRequester.class)
                .chatModel(CHAT_MODEL)
                .async(true)
                .tools(new OrganizingTools())
                .outputKey("sentEmailId")
                .build();

        // 2. Build async conditional workflow
        UntypedAgent candidateResponder = AgenticServices
                .conditionalBuilder()
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.score >= 0.8; // if HR passes, send to manager for review
                }, managerCvReviewer)
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.score < 0.8; // if HR does not pass, send rejection email
                }, emailAssistant)
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.feedback.toLowerCase().contains("missing information:");
                }, infoRequester) // if needed, request more info from candidate
                .output(agenticScope ->
                        (agenticScope.readState("managerReview", new CvReview(0, "no manager review needed"))).toString() +
                                "\n" + agenticScope.readState("sentEmailId", 0)
                ) // final output is the manager review (if any)
                .build();

        // 3. Input arguments
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        CvReview hrReview = new CvReview(
                0.85,
                """
                        Solid candidate, salary expectations in scope and able to start within desired timeframe.
                        Missing information: details about work authorization status in Belgium.
                        """
        );

        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "jobDescription", jobDescription,
                "cvReview", hrReview
        );


        // 4. Run the conditional async workflow
        candidateResponder.invoke(arguments);

        System.out.println("=== Finished execution of async conditional workflow ===");
    }
}
