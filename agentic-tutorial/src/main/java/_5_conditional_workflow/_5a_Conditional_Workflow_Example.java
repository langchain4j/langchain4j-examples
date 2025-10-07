package _5_conditional_workflow;

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

public class _5a_Conditional_Workflow_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 200);  // control how much you see from the model calls
    }

    /**
     * This example demonstrates the conditional agent workflow.
     * Based on a score and a candidate profile, we will either
     * - invoke an agent that prepares everything for an on-site interview with the candidate
     * - invoke an agent that sends a kind email that we will not move forward*
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. Define the two sub-agents in this package:
        //      - EmailAssistant.java
        //      - InterviewOrganizer.java

        // 3. Create all agents using AgenticServices
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools()) // the agent can use all tools defined there
                .build();
        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .contentRetriever(RagProvider.loadHouseRulesRetriever()) // this is how we can add RAG to an agent
                .build();

        // 4. Build the conditional workflow
        UntypedAgent candidateResponder = AgenticServices // use UntypedAgent unless you define the resulting composed agent, see _2_Sequential_Agent_Example
                .conditionalBuilder()
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score >= 0.8, interviewOrganizer)
                .subAgents(agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score < 0.8, emailAssistant)
                .build();
        // Good to know: when multiple conditions are defined, they are all executed in sequence.
        // If you want parallel execution here, use async agents, as demonstrated in _5b_Conditional_Workflow_Example_Async

        // 5. Load the arguments from text files in resources/documents/
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        CvReview cvReviewFail = new CvReview(0.6, "The CV is good but lacks some technical details relevant for the backend position.");
        CvReview cvReviewPass = new CvReview(0.9, "The CV is excellent and matches all requirements for the backend position.");

        // 5. Because we use an untyped agent, we need to pass a map of all input arguments
        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "jobDescription", jobDescription,
                "cvReview", cvReviewPass // change to cvReviewFail to see the other branch
        );

        // 5. Call the conditional agent to respond to the candidate in line with the review
        candidateResponder.invoke(arguments);
        // in this example, we didn't make meaningful changes to the AgenticScope
        // and we don't have a meaningful output to print, since the tools executed the final action.
        // we print to the console which actions were taken by the tools (emails sent, application status updated)

        // when you observe the logs in debug mode, the tool call result 'success' is still sent to the model
        // and the model still answers something like "The email has been sent to John Doe informing him ..."

        // For info: if tools are your last actions and you don't want to call the model back afterwards,
        // you will typically add @Tool(returnBehavior = ReturnBehavior.IMMEDIATE)`
        // https://docs.langchain4j.dev/tutorials/tools#returning-immediately-the-result-of-a-tool-execution-request
        // !!! BUT in agentic workflows IMMEDIATE RETURN BEHAVIOR for tools is NOT RECOMMENDED,
        // since immediate return behavior will store the tool result in the AgenticScope and things can go wrong

        // For info: this was an example of routing behavior with a code check on the conditions.
        // Routing behavior can also be obtained by letting an LLM determine the best tool(s)/agent(s)
        // to continue with, either by using
        // - Supervisor agent: will operate on agents, see _7_supervisor_orchestration
        // - AiServices as tools, like this
        // RouterService routerService = AiServices.builder(RouterAgent.class)
        //        .chatModel(model)
        //        .tools(medicalExpert, legalExpert, technicalExpert)
        //        .build();
        //
        // The best option depends on your use case:
        //
        // - With conditional agents, you hardcode call criteria
        // - Vs. with AiServices or Supervisor, the LLM decide which expert(s) to call
        //
        // - With agentic solutions (conditional, supervisor) all intermediary states and the call chain are stored in AgenticScope
        // - Vs. with AiServices it is much harder to track the call chain or intermediary states

    }
}