package dev.langchain4j.example.booking;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class BookingService {

    private static final Customer CUSTOMER = new Customer("John", "Doe");

    private static final String BOOKING_NUMBER = "MS-777";
    private static final Booking BOOKING = new Booking(
            BOOKING_NUMBER,
            LocalDate.of(2025, 12, 13),
            LocalDate.of(2025, 12, 31),
            CUSTOMER
    );

    private static final Map<String, Booking> BOOKINGS = new HashMap<>() {{
        put(BOOKING_NUMBER, BOOKING);
    }};

    public Booking getBookingDetails(String bookingNumber, String customerName, String customerSurname) {
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating DB lookup
        return BOOKINGS.get(bookingNumber);
    }

    public void cancelBooking(String bookingNumber, String customerName, String customerSurname) {
        ensureExists(bookingNumber, customerName, customerSurname);

        // Imitating booking cancellation
        BOOKINGS.remove(bookingNumber);
    }

    private void ensureExists(String bookingNumber, String customerName, String customerSurname) {
        // Imitating DB lookup

        Booking booking = BOOKINGS.get(bookingNumber);
        if (booking == null) {
            throw new BookingNotFoundException(bookingNumber);
        }

        Customer customer = booking.customer();
        if (!customer.name().equals(customerName)) {
            throw new BookingNotFoundException(bookingNumber);
        }
        if (!customer.surname().equals(customerSurname)) {
            throw new BookingNotFoundException(bookingNumber);
        }
    }
}
