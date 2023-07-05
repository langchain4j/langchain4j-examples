package dev.example;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingTools {

    @Autowired
    private BookingService bookingService;

    @Tool
    public Booking getBookingDetails(String bookingNumber, String customerName, String customerSurname) {
        System.out.printf("[Tool]: getting details for booking %s for %s %s...%n", bookingNumber, customerName, customerSurname);
        return bookingService.getBookingDetails(bookingNumber, customerName, customerSurname);
    }

    @Tool
    public void cancelBooking(String bookingNumber, String customerName, String customerSurname) {
        System.out.printf("[Tool]: cancelling booking %s for %s %s...%n", bookingNumber, customerName, customerSurname);
        bookingService.cancelBooking(bookingNumber, customerName, customerSurname);
    }
}