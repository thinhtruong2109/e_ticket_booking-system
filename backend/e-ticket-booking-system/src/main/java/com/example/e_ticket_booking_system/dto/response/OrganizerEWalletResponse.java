package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerEWalletResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private BigDecimal balance;
    private BigDecimal totalWithdrawn;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
