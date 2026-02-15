package com.example.e_ticket_booking_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private boolean success;
    private String message;
    private String ticketCode;
    private String eventName;
    private String seatInfo;
    private String ownerName;
}
