package com.example.e_ticket_booking_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        List<Venue> venues = venueRepository.findAll();
        List<VenueResponse> responseList = new ArrayList<>();
        for (Venue venue : venues) {
            VenueResponse response = toResponse(venue);
            responseList.add(response);
        }
        return responseList;
    }

    public VenueResponse getVenueById(Long id) {
        Optional<Venue> optionalVenue = venueRepository.findById(id);
        if (!optionalVenue.isPresent()) {
            throw new ResourceNotFoundException("Venue not found with id: " + id);
        }
        Venue venue = optionalVenue.get();
        return toResponse(venue);
    }

    public List<VenueResponse> getVenuesByCity(String city) {
        List<Venue> venues = venueRepository.findByCity(city);
        List<VenueResponse> responseList = new ArrayList<>();
        for (Venue venue : venues) {
            VenueResponse response = toResponse(venue);
            responseList.add(response);
        }
        return responseList;
    }

    public VenueResponse createVenue(CreateVenueRequest request) {
        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setCountry(request.getCountry());
        venue.setTotalCapacity(request.getTotalCapacity());

        // Xác định hasSeatMap: mặc định là false
        if (request.getHasSeatMap() != null) {
            venue.setHasSeatMap(request.getHasSeatMap());
        } else {
            venue.setHasSeatMap(false);
        }

        venue = venueRepository.save(venue);
        log.info("Venue created: {}", venue.getName());
        return toResponse(venue);
    }

    public VenueResponse updateVenue(Long id, CreateVenueRequest request) {
        Optional<Venue> optionalVenue = venueRepository.findById(id);
        if (!optionalVenue.isPresent()) {
            throw new ResourceNotFoundException("Venue not found with id: " + id);
        }
        Venue venue = optionalVenue.get();

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
