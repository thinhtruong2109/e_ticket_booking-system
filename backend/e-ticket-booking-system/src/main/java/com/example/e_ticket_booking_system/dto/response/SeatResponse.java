package com.example.e_ticket_booking_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private Long venueId;
    private Long sectionId;
    private String sectionName;
    private String rowNumber;
    private String seatNumber;
    private String seatType;
    private boolean available;
}
