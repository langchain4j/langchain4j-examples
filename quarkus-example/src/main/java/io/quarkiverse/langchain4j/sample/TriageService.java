package io.quarkiverse.langchain4j.sample;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface TriageService {

    @SystemMessage("""
            You are working for a bank. You are an AI processing reviews about financial products. You need to triage the reviews into positive and negative ones.
            You will always answer with a JSON document, and only this JSON document.
            """)
    @UserMessage("""
            Your task is to process the review delimited by ---.
            Apply a sentiment analysis to the passed review to determine if it is positive or negative.
            The review can be in any language. So, you will need to identify the language.

            For example:
            - "I love your bank, you are the best!", this is a 'POSITIVE' review
            - "J'adore votre banque", this is a 'POSITIVE' review
            - "I hate your bank, you are the worst!", this is a 'NEGATIVE' review

             Answer with a JSON document containing:
            - the 'evaluation' key set to 'POSITIVE' if the review is positive, 'NEGATIVE' otherwise, depending if the review is positive or negative
            - the 'message' key set to a message thanking the customer in the case of a positive review, or an apology and a note that the bank is going to contact the customer in the case of a negative review. These messages must be polite and use the same language as the passed review.

            ---
            {review}
            ---
            """)
    TriagedReview triage(String review);

}
