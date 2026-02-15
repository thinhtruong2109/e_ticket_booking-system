package com.example.e_ticket_booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.request.CreateTicketExchangeRequest;
import com.example.e_ticket_booking_system.dto.request.CreateTicketListingRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.TicketExchangeResponse;
import com.example.e_ticket_booking_system.dto.response.TicketListingResponse;
import com.example.e_ticket_booking_system.service.TicketExchangeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-listings")
@RequiredArgsConstructor
public class TicketListingController {

    private final TicketExchangeService exchangeService;
    private final SecurityUtils securityUtils;

    // Listing endpoints
    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketListingResponse>>> getActiveListings() {
        return ResponseEntity.ok(ApiResponse.success(exchangeService.getActiveListings()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketListingResponse>> getListingById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(exchangeService.getListingById(id)));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<ApiResponse<List<TicketListingResponse>>> getMyListings() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(exchangeService.getListingsBySeller(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketListingResponse>> createListing(
            @Valid @RequestBody CreateTicketListingRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Listing created",
                exchangeService.createListing(userId, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelListing(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        exchangeService.cancelListing(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Listing cancelled"));
    }

    // Exchange endpoints
    @PostMapping("/exchanges")
    public ResponseEntity<ApiResponse<TicketExchangeResponse>> createExchange(
            @Valid @RequestBody CreateTicketExchangeRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Exchange request created",
                exchangeService.createExchange(userId, request)));
    }

    @PutMapping("/exchanges/{id}/complete")
    public ResponseEntity<ApiResponse<TicketExchangeResponse>> completeExchange(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Exchange completed",
                exchangeService.completeExchange(id)));
    }

    @DeleteMapping("/exchanges/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelExchange(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        exchangeService.cancelExchange(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Exchange cancelled"));
    }
}
