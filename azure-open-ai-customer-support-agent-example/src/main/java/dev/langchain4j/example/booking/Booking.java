package dev.langchain4j.example.booking;

import java.time.LocalDate;

public class Booking {

    private String bookingNumber;
    private LocalDate bookingFrom;
    private LocalDate bookingTo;
    private Customer customer;

    public Booking(String bookingNumber, LocalDate bookingFrom, LocalDate bookingTo, Customer customer) {
        this.bookingNumber = bookingNumber;
        this.bookingFrom = bookingFrom;
        this.bookingTo = bookingTo;
        this.customer = customer;
    }
}