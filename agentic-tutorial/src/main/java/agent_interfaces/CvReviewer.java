package agent_interfaces;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import model.CvReview;

public interface CvReviewer {

    @Agent("Reviews a CV according to specific instructions, gives feedback and a score")
    @SystemMessage("""
            You are the hiring manager for this job:Here is a CV that needs tailoring to a specific job description, feedback or other instruction.
            {{jobDescription}}
            Your review applicant CVs and need to decide who of the many applicants you invite for an on-site interview.
            You give each CV a score and feedback (both the good and the bad things).
            You can ignore things like missing address and placeholders.
            """)
    @UserMessage("""
            Review this CV: {{tailoredCv}}
            """)
    CvReview reviewCv(@V("tailoredCv") String tailoredCv, @V("jobDescription") String jobDescription);
}
