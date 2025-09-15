package _9_human_in_the_loop;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import util.ChatModelProvider;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.util.Map;

public class _9b_HumanInTheLoop_Chatbot_With_Memory {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);  // control how much you see from the model calls
    }

    /**
     * This example demonstrates a back-and-forth loop with human-in-the-loop interaction,
     * until an end-goal is reached (exit condition), after which the rest of the workflow
     * can continue.
     * The loop continues until the human confirms availability, which is verified by an AiService.
     * When no slot is found, the loop ends after 5 iterations.
     */

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {

        // 1. Define sub-agent
        MeetingProposer proposer = AgenticServices
                .agentBuilder(MeetingProposer.class)
                .chatModel(CHAT_MODEL)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15)) // so the agent remembers what he proposed already
                .outputName("proposal")
                .build();

        // 2. Add an AiService to judge if a decision has been reached (this can be a tiny local model because the assignment is so simple)
        DecisionsReachedService decisionService = AiServices.create(DecisionsReachedService.class, CHAT_MODEL);

        // 2. Define Human-in-the-loop agent
        HumanInTheLoop humanInTheLoop = AgenticServices
                .humanInTheLoopBuilder()
                .description("agent that asks input from the user")
                .outputName("candidateAnswer") // matches one of the proposer's input variable names
                .inputName("proposal") // must match the output of the proposer agent
                .requestWriter(request -> {
                    System.out.println(request);
                    System.out.print("> ");
                })
                .responseReader(() -> System.console().readLine())
                .build();

        // 3. construct the loop
        UntypedAgent schedulingLoop = AgenticServices
                .loopBuilder()
                .subAgents(proposer, humanInTheLoop)
                .exitCondition(scope -> {
                    String response = (String) scope.readState("candidateAnswer");
                    String proposal = (String) scope.readState("proposal");
                    return response != null && decisionService.isDecisionReached(proposal, response);
                })
                .maxIterations(5)
                .output(agenticScope -> Map.of(
                        "proposal", agenticScope.readState("proposal"),
                        "candidateAnswer", agenticScope.readState("candidateAnswer")
                ))
                // this output contains the last date proposal + candidate's answer, which should be sufficient info for a followup agent to schedule the meeting (or abort trying)
                .build();

        // 4. Run the scheduling loop
        Map<String, Object> input = Map.of("meetingTopic", "on-site visit",
                "candidateAnswer", "hi", // this variable needs to be present in the AgenticScope in advance because the MeetingProposer takes it as input
                "memoryId", "user-1234"); // if we don't put a memoryId, the proposer agent will not remember what he proposed already

        var lastProposalAndAnswer = schedulingLoop.invoke(input);

        System.out.println("\n=== RETAINED MEETING SLOT OR FAILURE TO FIND A SLOT IN 5 ITERATIONS===");
        System.out.println(lastProposalAndAnswer);
    }
}
