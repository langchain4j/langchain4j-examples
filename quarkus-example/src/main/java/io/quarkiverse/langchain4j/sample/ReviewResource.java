package io.quarkiverse.langchain4j.sample;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/review")
public class ReviewResource {

    @Inject
    TriageService triage;

    record Review(String review) {
    }

    @POST
    public TriagedReview triage(Review review) {
        return triage.triage(review.review());
    }

}
