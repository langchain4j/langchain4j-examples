package _1_basic_agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.Cv;

public interface CvGeneratorStructuredOutput {
    @UserMessage("""
            Here is information on my life and professional trajectory
            that you should turn into a clean and complete CV.
            Do not invent facts and do not leave out skills or experiences.
            This CV will later be cleaned up, for now, make sure it is complete.
            My life story: {{lifeStory}}
            """)
    @Agent("Generates a clean CV based on user-provided information")
    Cv generateCv(@V("lifeStory") String userInfo);
}
