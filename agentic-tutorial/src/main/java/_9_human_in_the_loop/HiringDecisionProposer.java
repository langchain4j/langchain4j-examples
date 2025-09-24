package _9_human_in_the_loop;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface HiringDecisionProposer {
    
    @Agent("Summarizes hiring decision for final validation")
    @SystemMessage("""
        You summarize the hiring reasons in 3 lines max for a given review,
        for a human to make the final decision whether to proceed or not.
        """)
    @UserMessage("""
        Feedback from all parties involved in the hiring process: {{cvReview}}
        """)
    String propose(@V("cvReview") CvReview cvReview);
}
