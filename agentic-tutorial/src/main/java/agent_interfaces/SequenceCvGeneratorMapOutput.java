package agent_interfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

import java.util.Map;

public interface SequenceCvGeneratorMapOutput {
    @Agent("Generates a CV based on user-provided information and tailored to instructions")
    Map<String, String> generateTailoredCv(@V("userInfo") String userInfo, @V("instructions") String instructions);
}
