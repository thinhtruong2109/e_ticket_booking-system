package com.example.e_ticket_booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.request.CheckInRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.CheckInResponse;
import com.example.e_ticket_booking_system.dto.response.TicketResponse;
import com.example.e_ticket_booking_system.service.TicketService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SecurityUtils securityUtils;

    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTickets() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(ticketService.getMyTickets(userId)));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketsByBooking(bookingId)));
    }

    @GetMapping("/code/{ticketCode}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByCode(@PathVariable String ticketCode) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketByCode(ticketCode)));
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN', 'ORGANIZER')")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(@Valid @RequestBody CheckInRequest request) {
        Long staffId = securityUtils.getCurrentUserId();
        CheckInResponse response = ticketService.checkIn(staffId, request);
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }
}
