package com.example.e_ticket_booking_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionResponse {
    private Long id;
    private Long venueId;
    private String name;
    private String description;
    private Integer capacity;
    private Boolean hasNumberedSeats;
}
