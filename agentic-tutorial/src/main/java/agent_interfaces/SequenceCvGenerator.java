package agent_interfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SequenceCvGenerator {
    @Agent("Generates a CV based on user-provided information and tailored to instructions")
    String generateTailoredCv(@V("userInfo") String userInfo, @V("instructions") String instructions);
}
