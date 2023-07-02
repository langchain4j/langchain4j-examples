package dev.example;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static java.time.Month.JANUARY;

@Component
public class BookingService {

    public Booking getBooking(String bookingNumber, String customerName, String customerSurname) {
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating retrieval from DB
        LocalDate bookingFrom = LocalDate.of(2024, JANUARY, 1);
        LocalDate bookingTo = LocalDate.of(2024, JANUARY, 31);
        Customer customer = new Customer(customerName, customerSurname);
        return new Booking(bookingNumber, bookingFrom, bookingTo, customer);
    }

    public void cancelBooking(String bookingNumber, String customerName, String customerSurname) {
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating cancellation
    }

    private void ensureExists(String bookingNumber, String customerName, String customerSurname) {
        // Imitating check
        if (!bookingNumber.equals("123-456")) {
            throw new BookingNotFoundException(bookingNumber);
        }
    }
}
