import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

import static java.time.Month.JANUARY;

public class ServiceWithToolsExample {

    static class Booking {

        String bookingNumber;
        LocalDate bookingFrom;
        LocalDate bookingTo;
        Customer customer;
    }

    static class Customer {

        String name;
        String surname;
    }

    static class BookingTools {

        @Tool("Get information about booking")
        public Booking getBooking(String bookingNumber, String customerName, String customerSurname) {
            // Imitating retrieval from DB
            Booking booking = new Booking();
            booking.bookingNumber = bookingNumber;
            booking.bookingFrom = LocalDate.of(2024, JANUARY, 1);
            booking.bookingTo = LocalDate.of(2024, JANUARY, 31);
            booking.customer = new Customer();
            booking.customer.name = customerName;
            booking.customer.surname = customerSurname;
            return booking;
        }

        @Tool("Cancel booking")
        public void cancelBooking(String bookingNumber, String customerName, String customerSurname) {
            // Imitating cancellation
        }
    }

    interface CustomerSupportAgent {

        @SystemMessage({
                "You are a customer support agent of a car rental company named 'Miles of Smiles'.",
                "Before providing information about booking or cancelling booking, you must always check:",
                "booking number, customer name and surname."
        })
        String chat(String message);
    }

    public static void main(String[] args) throws IOException {

        String apiKey = System.getenv("OPENAI_API_KEY"); // https://platform.openai.com/account/api-keys
        ChatLanguageModel chatLanguageModel = OpenAiChatModel.withApiKey(apiKey);

        ChatMemory chatMemory = MessageWindowChatMemory.withCapacity(20);

        BookingTools bookingTools = new BookingTools();

        CustomerSupportAgent customerSupportAgent = AiServices.builder(CustomerSupportAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(bookingTools)
                .build();

        // Now, you can ask the agent to provide information about the booking or cancel it

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("User: ");
            String messageFromUser = br.readLine();

            if ("exit".equalsIgnoreCase(messageFromUser)) {
                return;
            }

            String responseFromAI = customerSupportAgent.chat(messageFromUser);
            System.out.println("Customer support agent: " + responseFromAI);
        }
    }
}
