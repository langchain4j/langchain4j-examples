package _2_sequential_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;
import dev.langchain4j.service.V;

import java.util.Map;

public interface SequenceCvGenerator {
    @Agent("Generates a CV based on user-provided information and tailored to instructions, don't make it too long, avoid empty lines")
    ResultWithAgenticScope<Map<String, String>> generateTailoredCv(@V("lifeStory") String lifeStory, @V("instructions") String instructions);
}
