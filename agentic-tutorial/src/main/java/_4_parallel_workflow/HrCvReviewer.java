package _4_parallel_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface HrCvReviewer {

    @Agent("Reviews a CV to check if candidate fits HR requirements, gives feedback and a score")
    @SystemMessage("""
            You are working for HR and review CVs to fill a position with these requirements:
            {{hrRequirements}}
            You give each CV a score and feedback (both the good and the bad things).
            You can ignore things like missing address and placeholders.
            """)
    @UserMessage("""
            Review this CV: {{candidateCv}} with accompanying phone interview notes: {{phoneInterviewNotes}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv, @V("phoneInterviewNotes") String phoneInterviewNotes, @V("hrRequirements") String hrRequirements);
}
