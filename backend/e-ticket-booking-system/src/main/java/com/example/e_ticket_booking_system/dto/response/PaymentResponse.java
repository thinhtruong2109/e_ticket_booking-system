package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long bookingId;
    private String bookingCode;
    private String paymentMethod;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
