package _5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface InfoRequester {

    @Agent("Emails a candidate to obtain extra info")
    @SystemMessage("""
            You send a kind email to candidates to request extra information the company needs
            in order to review the application. Make clear that their application is still being considered.
            """)
    @UserMessage("""
            HR review with description of missing info: {{cvReview}}
            
            Candidate contact info: {{candidateContact}}
            
            Job description: {{jobDescription}}
            """)
    String send(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription, @V("cvReview") CvReview hrReview);
}
