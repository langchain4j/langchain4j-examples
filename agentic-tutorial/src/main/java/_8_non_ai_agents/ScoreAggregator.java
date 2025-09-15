package _8_non_ai_agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;
import domain.CvReview;

/**
 * Non-AI agent that aggregates multiple CV reviews into a combined review.
 * This demonstrates how plain Java operators can be used as first-class agents
 * in agentic workflows, making them interchangeable with AI-powered agents.
 */
public class ScoreAggregator {

    @Agent(description = "Aggregates HR/Manager/Team reviews into a combined review", outputName = "combinedCvReview")
    public CvReview aggregate(@V("hrReview") CvReview hr,
                             @V("managerReview") CvReview mgr,
                             @V("teamMemberReview") CvReview team) {

        System.out.println("ScoreAggregator called with hrReview: " + hr +
                ", managerReview: " + mgr +
                ", teamMemberReview: " + team);

        double avgScore = (hr.score + mgr.score + team.score) / 3.0;
        
        String combinedFeedback = String.join("\n\n",
                "HR Review: " + hr.feedback,
                "Manager Review: " + mgr.feedback,
                "Team Member Review: " + team.feedback
        );
        
        return new CvReview(avgScore, combinedFeedback);
    }
}

