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
            You also invite the candidate with a congratulatory email and interview details.
            Lastly, you update the application status to 'invited on-site'.
            """)
    @UserMessage("""
            Organize the interview for this job: {{jobDescription}}
            With this candidate: {{candidateContact}}
            """)
    String organize(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription);
}
