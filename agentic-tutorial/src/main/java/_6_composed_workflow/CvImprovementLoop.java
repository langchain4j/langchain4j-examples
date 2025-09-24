package _6_composed_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

public interface CvImprovementLoop {
    @Agent("Improves CV through iterative tailoring and review until passing score")
    String improveCv(@V("cv") String cv, @V("jobDescription") String jobDescription);
}
