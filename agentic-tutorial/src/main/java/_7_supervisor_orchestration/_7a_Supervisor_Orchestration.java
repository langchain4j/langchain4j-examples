package _7_supervisor_orchestration;

import _4_parallel_workflow.HrCvReviewer;
import _4_parallel_workflow.ManagerCvReviewer;
import _4_parallel_workflow.TeamMemberCvReviewer;
import _5_conditional_workflow.EmailAssistant;
import _5_conditional_workflow.InterviewOrganizer;
import _5_conditional_workflow.OrganizingTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.model.chat.ChatModel;
import util.ChatModelProvider;
import util.StringLoader;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;

/**
 * Up until now we built deterministic workflows:
 * - sequential, parallel, conditional, loop, and compositions of those.
 * You can also build a Supervisor agentic system, in which an agent will
 * decide dynamically which of his sub-agents to call in which order..
 * In this example, the Supervisor coordinates the hiring workflow:
 * He is supposed to runs HR/Manager/Team reviews and either schedule
 * an interview or send a rejection email.
 * Just like part 2 of the Composed Workflow example, but now 'self-organised'
 * Note that supervisor super-agents can be used in composed workflows just like the other super- agent types.
 */
public class _7a_Supervisor_Orchestration {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 200);  // control how much you see from the model calls
    }

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 1. Define all sub-agents
        HrCvReviewer hrReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("hrReview")
                .build();
        // importantly, if we use the same method names for multiple agents
        // (in this case: 'reviewCv' for all reviewers) we need to name our agents, like this:
        // @Agent(name = "managerReviewer", description = "Reviews a CV based on a job description, gives feedback and a score")
        // TODO Mario is this really desired behavior? even with different signatures and descriptions?
        // TODO log excerpt when I named one of the three:
        // The comma separated list of available agents is:
        //'{reviewCv: Reviews a CV to see if candidate fits in the team, gives feedback and a score, [candidateCv]},
        // {organize: Organizes on-site interviews with applicants, [candidateContact, jobDescription]},
        // {hrReviewer: Reviews a CV to check if candidate fits HR requirements, gives feedback and a score, [candidateCv, phoneInterviewNotes, hrRequirements]},
        // {send: Sends rejection emails to candidates that didn't pass, [candidateContact, jobDescription]}'

        ManagerCvReviewer managerReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("managerReview")
                .build();

        TeamMemberCvReviewer teamReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(CHAT_MODEL)
                .outputName("teamMemberReview")
                .build();

        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .build();

        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .build();

        // 2. Build the Supervisor agent
        SupervisorAgent hiringSupervisor = AgenticServices.supervisorBuilder()
                .chatModel(CHAT_MODEL)
                .subAgents(hrReviewer, managerReviewer, teamReviewer, interviewOrganizer, emailAssistant)
                .contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION)
                .responseStrategy(SupervisorResponseStrategy.SUMMARY) // we want a summary of what happened, rather than retrieving a response
                .supervisorContext("Always use the full panel of available reviewers. Always answer in English. When invoking agent, use pure JSON (no backticks, and new lines as backslash+n).") // optional context for the supervisor on how to behave
                .build();
        // TODO Mario: is parallel execution possible here? it seems not, would be nice to have

        // 3. Load candidate CV & job description
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");

        // start a timer
        long start = System.nanoTime();
        // 4. Invoke Supervisor with a natural request
        String result = (String) hiringSupervisor.invoke(
                "Evaluate the following candidate:\n" +
                        "Candidate CV:\n" + candidateCv + "\n\n" +
                        "Candidate Contacts:\n" + candidateContact + "\n\n" +
                        "Job Description:\n" + jobDescription + "\n\n" +
                        "HR Requirements:\n" + hrRequirements + "\n\n" +
                        "Phone Interview Notes:\n" + phoneInterviewNotes
        );
        long end = System.nanoTime();
        double elapsedSeconds = (end - start) / 1_000_000_000.0;  // Convert to seconds

        System.out.println("=== SUPERVISOR RUN COMPLETED in " + elapsedSeconds + " seconds ===");
        System.out.println(result);
    }

    // ADVANCED USE CASES:
    // See _7b_Supervisor_Orchestration_Advanced.java for
    // - typed supervisor,
    // - context engineering,
    // - output strategies,
    // - call chain observation,

    // ON LATENCY:
    // The whole run of this flow typically takes over 60s.
    // A solution for this is to use a fast inference provider like CEREBRAS,
    // which will run the whole flow in 10s but makes more mistakes.
    // To try this example with CEREBRAS, get a key (click get started with free API key)
    // https://inference-docs.cerebras.ai/quickstart
    // and save in env variables as "CEREBRAS_API_KEY"
    // Then change line 38 to:
    // private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel("CEREBRAS");

}
