package com.example.e_ticket_booking_system.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private LocalDateTime createdAt;
}
