package dev.example;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingTools {

    @Autowired
    private BookingService bookingService;

    @Tool("Get booking details")
    public Booking getBooking(String bookingNumber, String customerName, String customerSurname) {
        System.out.printf("[getting details for booking %s for %s %s]%n", bookingNumber, customerName, customerSurname);
        return bookingService.getBooking(bookingNumber, customerName, customerSurname);
    }

    @Tool("Cancel booking")
    public void cancelBooking(String bookingNumber, String customerName, String customerSurname) {
        System.out.printf("[cancelling booking %s for %s %s]%n", bookingNumber, customerName, customerSurname);
        bookingService.cancelBooking(bookingNumber, customerName, customerSurname);
    }
}