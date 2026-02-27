package com.example.e_ticket_booking_system.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;
    private String ticketCode;
    private Long bookingId;
    private String bookingCode;
    private Long eventId;
    private String eventName;
    private Long scheduleId;
    private String ticketTypeName;
    private String qrCode;
    private Long currentOwnerId;
    private String currentOwnerName;
    private Boolean isTransferable;
    private Boolean isCheckedIn;
    private LocalDateTime checkedInAt;
    private String checkedInByName;
    private LocalDateTime createdAt;
}
