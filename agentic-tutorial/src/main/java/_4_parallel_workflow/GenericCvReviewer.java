package _4_parallel_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface GenericCvReviewer {

    @Agent("Reviews a CV to check if candidate fits HR requirements, gives feedback and a score")
    @SystemMessage("{{reviewInstructions}}")
    @UserMessage("Review this content according to your instructions: {{content}}")
    // TODO Mario: directly annotating UserMessage / SystemMessge here does not work like AiServices?
    CvReview reviewCv(@V("content") String content, @V("reviewInstructions") String reviewInstructions);
}
