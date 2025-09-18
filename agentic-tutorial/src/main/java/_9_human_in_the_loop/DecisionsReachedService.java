package _9_human_in_the_loop;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DecisionsReachedService {
    @SystemMessage("Given the interaction, return true if a decision has been reached, " +
            "false if further discussion is needed to find a solution.")
    @UserMessage("""
            Interaction so far:
             Secretary: {{proposal}}
             Invitee: {{candidateAnswer}}
    """)
    boolean isDecisionReached(@V("proposal") String proposal, @V("candidateAnswer") String candidateAnswer);
}

