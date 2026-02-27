package com.example.e_ticket_booking_system.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private Integer totalCapacity;
    private Boolean hasSeatMap;
    private LocalDateTime createdAt;
}
