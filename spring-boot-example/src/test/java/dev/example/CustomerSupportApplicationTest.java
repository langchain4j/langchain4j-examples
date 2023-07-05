package dev.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CustomerSupportApplicationTest {

    @Autowired
    CustomerSupportAgent agent;

    @Test
    void should_provide_booking_details_and_explain_why_cancellation_is_not_possible() {

        // Please define API keys in application.properties before running this test.
        // Tip: Use gpt-4 for this example, as gpt-3.5-turbo tends to hallucinate often and invent name and surname.

        interact(agent, "Hi, I forgot when my booking is.");
        interact(agent, "123-457");
        interact(agent, "I'm sorry I'm so inattentive today. Klaus Heisler.");
        interact(agent, "My bad, it's 123-456");
        interact(agent, "My plans have changed, can I cancel my booking?");

        // Here, information about the cancellation policy is automatically retrieved and injected into the LLM prompt.
        // Although the LLM still attempts to cancel the booking, it fails to do so and will explain the reason why
        // the booking cannot be cancelled, based on the cancellation policy.
    }

    private static void interact(CustomerSupportAgent agent, String userMessage) {
        System.out.println("[User]: " + userMessage);
        String agentAnswer = agent.chat(userMessage);
        System.out.println("[Agent]: " + agentAnswer);
    }
}
