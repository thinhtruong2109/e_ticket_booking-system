package com.example.e_ticket_booking_system.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private String description;
    private EventCategoryResponse category;
    private UserResponse organizer;
    private VenueResponse venue;
    private String bannerImageUrl;
    private String thumbnailImageUrl;
    private String status;
    private Integer totalTickets;
    private Integer availableTickets;
    private Boolean allowTicketExchange;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
