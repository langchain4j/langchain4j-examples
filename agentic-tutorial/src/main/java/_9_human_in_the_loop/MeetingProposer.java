package _9_human_in_the_loop;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface MeetingProposer {
    
    @Agent("Proposes a meeting time")
    @SystemMessage("""
        You assist CompanyA in trying to schedule a new meeting on topic {{meetingTopic}}.
        Reserve 3 hours for the meeting.
        
        You propose the candidate a meeting slot in a single phrase, like:
        "Would you be available next Monday at 10am?"
        Also answer user questions if there are any.
        
        Your team has following meeting availability: next week Mon, Tue or Thu at 9am,
        or the week after that, Tue, Wed or Fri at 2pm.
        Today is {{current_date}}..
        """)
    @UserMessage("""
        Previous candidate answer was: {{candidateAnswer}}
        """)
    String propose(@MemoryId String memoryId, @V("meetingTopic") String meetingTopic, @V("candidateAnswer") String candidateAnswer);
}
