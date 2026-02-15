package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // VNPAY, MOMO, STRIPE
}
