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

public class _5_Conditional_Workflow_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

     /**
     * This example demonstrates the conditional agent workflow.
     * Based on a score and a candidate profile, we will either
     * - invoke an agent that prepares everything for an on-site interview with the candidate
     * - invoke an agent that sends a kind email that we will not move forward*
     */

    // 1. Define the model that will power the agents
    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) throws IOException {

        // 2. Define the two sub-agents in this package:
        //      - EmailAssistant.java
        //      - InterviewOrganizer.java

        // 3. Create all agents using AgenticServices
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .build();
        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(CHAT_MODEL)
                .tools(new OrganizingTools())
                .build();

        // 4. Build the conditional workflow
        UntypedAgent candidateResponder = AgenticServices // use UntypedAgent unless you define the resulting compound agent, see _2_Sequential_Agent_Example
                .conditionalBuilder()
                .subAgents( agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score>=0.8, interviewOrganizer)
                .subAgents( agenticScope -> ((CvReview) agenticScope.readState("cvReview")).score<0.8, emailAssistant)
                .build();

        // 5. Load the arguments from text files in resources/documents/
        // - candidate_contacts.txt
        // - candidate_cv.txt
        // - job_description_backend.txt
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
        // and we don't have a meaningful output to print.
        // we print to the console which actions were taken by the tools (emails sent, application status updated)

        // when you observe the logs in debug mode, the tool call result 'success' is still sent to the model
        // and the model still answers something like "The email has been sent to John Doe informing him ..."

        // if you don't want the resource-wasteful sending of these tool results back to the model
        // you can add @Tool(returnBehavior = ReturnBehavior.IMMEDIATE)`
        // https://docs.langchain4j.dev/tutorials/tools#returning-immediately-the-result-of-a-tool-execution-request
        // (to be used with great care as it could cut the execution flow short if you need multiple tools to be called)




        // Note: a similar routing behavior can be obtained by using AiServices as tools, like this
        // RouterService routerService = AiServices.builder(RouterAgent.class)
        //        .chatModel(model)
        //        .tools(medicalExpert, legalExpert, technicalExpert)
        //        .build();
        // The best option depends on your use case:
        // With conditional agents, you hardcode call criteria
        // With AiServices, the LLM decide which expert(s) to call
        // With conditional agents, all intermediary states and the call chain are stored in AgenticScope
        // With AiServices it is much harder to track the call chain,
        // and there is no AgenticScope for intermediary parameters.

    }
}