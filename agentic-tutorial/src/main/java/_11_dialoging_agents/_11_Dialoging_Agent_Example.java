package _11_dialoging_agents;

import _3_loop_workflow.CvReviewer;
import _3_loop_workflow.ScoredCvTailor;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import domain.CvReview;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

public class _11_Dialoging_Agent_Example {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 100);  // control how much you see from the model calls
    }

    // TODO for the case where one wants a back and forth in one step (dialoging agent) + an exit condition (eg. finalDecisionReached or taskFulfilled) does one use supervisor or loop with one agent?
    /**
     * TODO
     */

    // In many cases, we want chatbot-like behavior for some of our sub-agents.
    // We want to have a back-and-forth with one agent until this agent decides it's job is done
    // TODO either do a 2 step loop: chat - check - chat - check and keep the memory in AgenticScope
    // or do an object (chatresponse + check)
    // TODO try a one-agent loop with a dialoging agent and an exit condition
            // TODO hitl as a second component in the loop? can the chatmemory be accessed if made part of the agenticscope for evaluation if goal is reached?

    // 1. Define the model that will power the agents
    private static final ChatModel CHAT_MODEL = OpenAiChatModel.builder().apiKey(System.getenv("OPENAI_API_KEY")).modelName(GPT_4_O_MINI).logRequests(true).logResponses(true).build();

    public static void main(String[] args) throws IOException {

        // 2. Define the sub-agents in this package:
        //      - ChatBot.java

        // 3. Create the agent using AgenticServices

        ChatBot chatBot = AgenticServices.agentBuilder(ChatBot.class)
                .chatModel(CHAT_MODEL)
                .outputName("chatResponse")
                .build();

        UntypedAgent dialogueLoop = AgenticServices
                .loopBuilder()
                .subAgents(chatBot)
                .outputName("chatResponse")
                .maxIterations(3)
                .build();
    }
}
