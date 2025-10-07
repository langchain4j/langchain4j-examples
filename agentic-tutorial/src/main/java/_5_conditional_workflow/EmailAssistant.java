package _5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface EmailAssistant {

    @Agent("Sends rejection emails to candidates that didn't pass")
    @SystemMessage("""
            You send a kind email to application candidates that did not pass the first review round.
            You also update the application status to 'rejected'.
            You return the sent email ID.
            """)
    @UserMessage("""
            Rejected candidate: {{candidateContact}}
            
            For job: {{jobDescription}}
            """)
    int send(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription);
}
