package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEventCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
    private String description;
    private String iconUrl;
}
