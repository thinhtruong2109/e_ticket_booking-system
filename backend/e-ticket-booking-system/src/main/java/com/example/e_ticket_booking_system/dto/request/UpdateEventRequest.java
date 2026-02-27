package com.example.e_ticket_booking_system.dto.request;

import lombok.Data;

@Data
public class UpdateEventRequest {
    private String name;
    private String description;
    private Long categoryId;
    private String bannerImageUrl;
    private String thumbnailImageUrl;
    private Boolean allowTicketExchange;
}
