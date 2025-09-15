package _2_sequential_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CvTailor {

    @Agent("Tailors a CV according to specific instructions")
    @SystemMessage("""
                Here is a CV that needs tailoring to a specific job description, feedback or other instruction.
                You can make the CV look good to meet the requirements, but don't invent facts.
                You can drop irrelevant things if it makes the CV better suited to the instructions.
                The goal is that the applicant gets an interview and can then live up to the CV. Don't make it overly long.
                The master CV: {{masterCv}}
                """)
    @UserMessage("""
                Here are the instructions for tailoring the CV: {{instructions}}
                """)
    String tailorCv(@V("masterCv") String masterCv, @V("instructions") String instructions);
}
