package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {
    private Long id;
    private Long paymentId;
    private Long userId;
    private String userFullName;
    private String transactionType;
    private String status;
    private BigDecimal amount;
    private String description;
    private String paymentMethod;
    private Long bookingId;
    private String bookingCode;
    private LocalDateTime createdAt;
}
