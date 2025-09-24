package _6_composed_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;


public interface CandidateWorkflow {
    @Agent("Based on life story and job description, generates master CV, tailors it to job description with feedback loop until passing score")
    String processCandidate(@V("lifeStory") String userInfo, @V("jobDescription") String jobDescription);
}
