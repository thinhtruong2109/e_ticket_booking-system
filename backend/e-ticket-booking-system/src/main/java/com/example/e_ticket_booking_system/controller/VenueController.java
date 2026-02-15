package com.example.e_ticket_booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.dto.request.CreateVenueRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.VenueResponse;
import com.example.e_ticket_booking_system.service.VenueService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getAllVenues() {
        return ResponseEntity.ok(ApiResponse.success(venueService.getAllVenues()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenueById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(venueService.getVenueById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VenueResponse>>> searchByCity(@RequestParam String city) {
        return ResponseEntity.ok(ApiResponse.success(venueService.getVenuesByCity(city)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(
            @Valid @RequestBody CreateVenueRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Venue created", venueService.createVenue(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<VenueResponse>> updateVenue(
            @PathVariable Long id, @Valid @RequestBody CreateVenueRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Venue updated", venueService.updateVenue(id, request)));
    }
}
