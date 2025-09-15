package _4_parallel_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import domain.CvReview;

public interface TeamMemberCvReviewer {

    @Agent(name = "teamMemberReviewer", description = "Reviews a CV to see if candidate fits in the team, gives feedback and a score")
    @SystemMessage("""
            You work in a team with motivated, self-driven colleagues and a lot of freedom.
            Your team values collaboration, responsibility and pragmatism.
            Your review applicant CVs and need to decide how well this person will fit in your team.
            You give each CV a score and feedback (both the good and the bad things).
            You can ignore things like missing address and placeholders.
            
            IMPORTANT: Return your response as valid JSON only, new lines as \\n, without any markdown formatting or code blocks.
            """)
    @UserMessage("""
            Review this CV: {{candidateCv}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv);
}
