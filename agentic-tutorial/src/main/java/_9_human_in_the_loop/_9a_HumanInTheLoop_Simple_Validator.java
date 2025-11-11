package _9_human_in_the_loop;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.model.chat.ChatModel;
import domain.CvReview;
import util.ChatModelProvider;
import util.log.CustomLogging;
import util.log.LogLevels;

import java.util.Map;
import java.util.Scanner;

public class _9a_HumanInTheLoop_Simple_Validator {

    static {
        CustomLogging.setLevel(LogLevels.PRETTY, 300);
    }

    private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel();

    public static void main(String[] args) {
        // 3. Create involved agents
        HiringDecisionProposer decisionProposer = AgenticServices.agentBuilder(HiringDecisionProposer.class)
                .chatModel(CHAT_MODEL)
                .outputKey("modelDecision")
                .build();

        // 2. Define human in the loop for validation
        HumanInTheLoop humanValidator = AgenticServices.humanInTheLoopBuilder()
                .description("validates the model's proposed hiring decision")
                .inputKey("modelDecision")
                .outputKey("finalDecision") // checked by human
                .requestWriter(request -> {
                    System.out.println("AI hiring assistant suggests: " + request);
                    System.out.println("Please confirm the final decision.");
                    System.out.println("Options: Invite on-site (I), Reject (R), Hold (H)");
                    System.out.print("> "); // we  needs input validation and error handling in real life systems
                })
                .responseReader(() -> new Scanner(System.in).nextLine())
                .build();

        // 3. Chain agents into a workflow
        UntypedAgent hiringDecisionWorkflow = AgenticServices.sequenceBuilder()
                .subAgents(decisionProposer, humanValidator)
                .outputKey("finalDecision")
                .build();

        // 4. Prepare input arguments
        Map<String, Object> input = Map.of(
                "cvReview", new CvReview(0.85,
                        """
                                Strong technical skills except for required React experience.
                                Seems a fast and independent learner though. Good cultural fit.
                                Potential issue with work permit that seems solvable.
                                Salary expectation slightly over planned budget.
                                Decision to proceed with onsite-interview.
                                """)
        );

        // 5. Run workflow
        String finalDecision = (String) hiringDecisionWorkflow.invoke(input);

        System.out.println("\n=== FINAL DECISION BY HUMAN ===");
        System.out.println("(Invite on-site (I), Reject (R), Hold (H))\n");
        System.out.println(finalDecision);

        // Note: human-in-the-loop and human validation can typically take long for the user to respond.
        // In this case, async agents are recommended so they don't block the rest of the workflow
        // that can potentially be executed before the user answer comes.
    }
}
