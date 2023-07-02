package dev.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CustomerSupportApplicationTest {

    @Autowired
    CustomerSupportAgent customerSupportAgent;

    @Test
    void should_provide_booking_details_and_cancel_booking() {
        String firstMessage = "Hi, I forgot when my booking is.";
        System.out.println("User: " + firstMessage);
        String firstAnswer = customerSupportAgent.chat(firstMessage);
        System.out.println("Agent: " + firstAnswer);

        String secondMessage = "123-457";
        System.out.println("User: " + secondMessage);
        String secondResponse = customerSupportAgent.chat(secondMessage);
        System.out.println("Agent: " + secondResponse);

        String thirdMessage = "I'm sorry I'm so inattentive today. Klaus Heisler.";
        System.out.println("User: " + thirdMessage);
        String thirdResponse = customerSupportAgent.chat(thirdMessage);
        System.out.println("Agent: " + thirdResponse);

        String fourthMessage = "My bad, it's 123-456.";
        System.out.println("User: " + fourthMessage);
        String fourthResponse = customerSupportAgent.chat(fourthMessage);
        System.out.println("Agent: " + fourthResponse);

        String fifthMessage = "Please cancel my booking as my plans have changed.";
        System.out.println("User: " + fifthMessage);
        String fifthResponse = customerSupportAgent.chat(fifthMessage);
        System.out.println("Agent: " + fifthResponse);
    }
}
