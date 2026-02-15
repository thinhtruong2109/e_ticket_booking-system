package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingCode;
    private Long customerId;
    private String customerName;
    private Long eventId;
    private String eventName;
    private Long scheduleId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String status;
    private LocalDateTime holdExpiresAt;
    private List<BookingDetailResponse> details;
    private LocalDateTime createdAt;
}
