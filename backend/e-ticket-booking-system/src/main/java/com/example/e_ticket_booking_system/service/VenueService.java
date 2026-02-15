package com.example.e_ticket_booking_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.CreateVenueRequest;
import com.example.e_ticket_booking_system.dto.response.VenueResponse;
import com.example.e_ticket_booking_system.entity.Venue;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VenueService {

    private static final Logger log = LoggerFactory.getLogger(VenueService.class);

    private final VenueRepository venueRepository;

    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public VenueResponse getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));
        return toResponse(venue);
    }

    public List<VenueResponse> getVenuesByCity(String city) {
        return venueRepository.findByCity(city).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public VenueResponse createVenue(CreateVenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setCountry(request.getCountry());
        venue.setTotalCapacity(request.getTotalCapacity());
        venue.setHasSeatMap(request.getHasSeatMap() != null ? request.getHasSeatMap() : false);

        venue = venueRepository.save(venue);
        log.info("Venue created: {}", venue.getName());
        return toResponse(venue);
    }

    public VenueResponse updateVenue(Long id, CreateVenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + id));

        if (request.getName() != null) venue.setName(request.getName());
        if (request.getAddress() != null) venue.setAddress(request.getAddress());
        if (request.getCity() != null) venue.setCity(request.getCity());
        if (request.getCountry() != null) venue.setCountry(request.getCountry());
        if (request.getTotalCapacity() != null) venue.setTotalCapacity(request.getTotalCapacity());
        if (request.getHasSeatMap() != null) venue.setHasSeatMap(request.getHasSeatMap());

        venue = venueRepository.save(venue);
        log.info("Venue updated: {}", venue.getName());
        return toResponse(venue);
    }

    private VenueResponse toResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(), venue.getName(), venue.getAddress(),
                venue.getCity(), venue.getCountry(), venue.getTotalCapacity(),
                venue.getHasSeatMap(), venue.getCreatedAt());
    }
}
