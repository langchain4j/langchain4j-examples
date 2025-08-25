package model;

import dev.langchain4j.model.output.structured.Description;

public class CvReview {
    @Description("Score from 0 to 1 how likely you would invite this candidate to an interview")
    public double score;

    @Description("Feedback on the CV, what is good, what needs improvement, what skills are missing, what red flags, ...")
    public String feedback;

    public CvReview() {} // no args constructor needed for deserialization, bcs other constructor is present!

    public CvReview(double score, String feedback) {
        this.score = score;
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "\nCvReview: " +
                " - score = " + score +
                "\n- feedback = \"" + feedback + "\"\n";
    }
}
