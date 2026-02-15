package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVenueRequest {
    @NotBlank(message = "Venue name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String city;
    private String country;
    private Integer totalCapacity;
    private Boolean hasSeatMap;
}
