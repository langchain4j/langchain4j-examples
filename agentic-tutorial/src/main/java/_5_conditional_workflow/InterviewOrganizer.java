package _5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface InterviewOrganizer {

    @Agent("Organizes on-site interviews with applicants")
    @SystemMessage("""
            You organize on-site meetings by sending a calendar invite to all implied employees
            for a 3h interview in one week from the current date, in the morning.
            This is the job opening in question: {{jobDescription}}
            You also invite the candidate with a congratulatory email, interview details 
            and anything he should be aware of before coming on-site.
            Lastly, you update the application status to 'invited on-site'.
            """)
    @UserMessage("""
            Organize an on-site interview meeting with this candidate (external visitor policy applies): {{candidateContact}}
            """)
    String organize(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription);
}
