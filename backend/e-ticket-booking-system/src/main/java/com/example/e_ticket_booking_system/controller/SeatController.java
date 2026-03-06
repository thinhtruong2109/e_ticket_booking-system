package com.example.e_ticket_booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.dto.request.BulkCreateSeatsRequest;
import com.example.e_ticket_booking_system.dto.request.CreateSeatRequest;
import com.example.e_ticket_booking_system.dto.request.CreateSectionRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.SeatResponse;
import com.example.e_ticket_booking_system.dto.response.SectionResponse;
import com.example.e_ticket_booking_system.service.SeatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // Section endpoints
    @PostMapping("/sections")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(
            @Valid @RequestBody CreateSectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Section created", seatService.createSection(request)));
    }

    @GetMapping("/sections/venue/{venueId}")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSectionsByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(ApiResponse.success(seatService.getSectionsByVenue(venueId)));
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable Long id, @Valid @RequestBody CreateSectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Section updated", seatService.updateSection(id, request)));
    }

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSection(@PathVariable Long id) {
        seatService.deleteSection(id);
        return ResponseEntity.ok(ApiResponse.success("Section deleted", null));
    }

    // Seat endpoints
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SeatResponse>> createSeat(@Valid @RequestBody CreateSeatRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Seat created", seatService.createSeat(request)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> bulkCreateSeats(
            @Valid @RequestBody BulkCreateSeatsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Seats created", seatService.bulkCreateSeats(request)));
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatsByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(ApiResponse.success(seatService.getSeatsByVenue(venueId)));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getAvailableSeats(
            @RequestParam Long scheduleId) {
        return ResponseEntity.ok(ApiResponse.success(seatService.getAvailableSeats(scheduleId)));
    }
}
