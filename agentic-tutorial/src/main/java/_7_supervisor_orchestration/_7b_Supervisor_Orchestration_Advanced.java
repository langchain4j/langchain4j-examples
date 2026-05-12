package _7_supervisor_orchestration;

import _4_parallel_workflow.HrCvReviewer;
import _4_parallel_workflow.ManagerCvReviewer;
import _4_parallel_workflow.TeamMemberCvReviewer;
import _5_conditional_workflow.EmailAssistant;
import _5_conditional_workflow.InterviewOrganizer;
import _5_conditional_workflow.OrganizingTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Advanced Supervisor Example with explicit AgenticScope to inspect evolving context
 */
public class _7b_Supervisor_Orchestration_Advanced {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 200);
    }

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    /**
     * In this example we build a similar supervisor as in _7a_Supervisor_Orchestration,
     * but we explore a number of extra features of the Supervisor:
     * - typed supervisor,
     * - context engineering,
     * - output strategies,
     * - call chain observation,
     * - context evolution inspection
     */
    public static void main(String[] args) throws IOException {

        // 1. Define subagents
        HrCvReviewer hrReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .build();
        ManagerCvReviewer managerReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .build();
        TeamMemberCvReviewer teamReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .build();
        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .outputKey("response")
                .build();
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .outputKey("response")
                .build();

        // 2. Build supervisor

        HiringSupervisor hiringSupervisor = AgenticServices
                .supervisorBuilder(HiringSupervisor.class)
                .chatModel(CHAT_MODEL)
                .subAgents(hrReviewer, managerReviewer, teamReviewer, interviewOrganizer, emailAssistant)
                .contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION)
                // depending on what your supervisor needs to know about what the sub-agents have been doing,
                // you can choose contextGenerationStrategy CHAT_MEMORY, SUMMARIZATION, or CHAT_MEMORY_AND_SUMMARIZATION
                .responseStrategy(SupervisorResponseStrategy.SCORED) // this strategy uses a scorer model to decide weather the LAST response or the SUMMARY solves the user request best
                // an output function here would override the response strategy
                .supervisorContext("Policy: Always check HR first, escalate if needed, reject low-fit.")
                .build();

        // 3. Load input data
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");

        String request = "Evaluate this candidate and either schedule an interview or send a rejection email.\n"
                + "Candidate CV:\n" + candidateCv + "\n"
                + "Candidate Contacts:\n" + candidateContact + "\n"
                + "Job Description:\n" + jobDescription + "\n"
                + "HR Requirements:\n" + hrRequirements + "\n"
                + "Phone Interview Notes:\n" + phoneInterviewNotes;

        // 4. Invoke supervisor
        long start = System.nanoTime();
        ResultWithAgenticScope<String> decision = hiringSupervisor.invoke(request, "Manager technical review is most important.");
        long end = System.nanoTime();

        System.out.println("=== Hiring Supervisor finished in " + ((end - start) / 1_000_000_000.0) + "s ===");
        System.out.println(decision.result());

        // Print collected contexts
        System.out.println("\n=== Context as Conversation ===");
        System.out.println(decision.agenticScope().contextAsConversation()); // will work in next release

    }
}
