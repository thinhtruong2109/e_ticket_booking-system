package com.example.e_ticket_booking_system.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BulkCreateSeatsRequest {

    @NotNull(message = "Venue ID is required")
    private Long venueId;

    private Long sectionId;

    private String seatType; // Default seat type for all rows (VIP, REGULAR, WHEELCHAIR)

    @NotEmpty(message = "At least one row is required")
    @Valid
    private List<RowSpec> rows;

    @Data
    public static class RowSpec {
        @NotNull(message = "Row label is required")
        private String rowLabel; // e.g. "A", "B", "1", "2"

        @NotNull(message = "Start number is required")
        @Positive
        private Integer startNumber; // e.g. 1

        @NotNull(message = "End number is required")
        @Positive
        private Integer endNumber; // e.g. 20

        private String seatType; // Override seat type per row
    }
}
