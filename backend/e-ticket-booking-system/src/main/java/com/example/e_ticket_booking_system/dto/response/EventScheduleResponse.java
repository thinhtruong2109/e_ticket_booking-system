package com.example.e_ticket_booking_system.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventScheduleResponse {
    private Long id;
    private Long eventId;
    private String eventName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private String status;
}
