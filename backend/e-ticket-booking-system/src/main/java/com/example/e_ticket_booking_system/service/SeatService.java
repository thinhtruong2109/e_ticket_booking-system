package com.example.e_ticket_booking_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.CreateSeatRequest;
import com.example.e_ticket_booking_system.dto.request.CreateSectionRequest;
import com.example.e_ticket_booking_system.dto.response.SeatResponse;
import com.example.e_ticket_booking_system.dto.response.SectionResponse;
import com.example.e_ticket_booking_system.entity.Seat;
import com.example.e_ticket_booking_system.entity.SeatReservation;
import com.example.e_ticket_booking_system.entity.Section;
import com.example.e_ticket_booking_system.entity.Venue;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.SeatRepository;
import com.example.e_ticket_booking_system.repository.SeatReservationRepository;
import com.example.e_ticket_booking_system.repository.SectionRepository;
import com.example.e_ticket_booking_system.repository.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatService {

    private static final Logger log = LoggerFactory.getLogger(SeatService.class);

    private final SeatRepository seatRepository;
    private final SectionRepository sectionRepository;
    private final VenueRepository venueRepository;
    private final SeatReservationRepository seatReservationRepository;

    // Section methods
    public SectionResponse createSection(CreateSectionRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.getVenueId()));

        Section section = new Section();
        section.setVenue(venue);
        section.setName(request.getName());
        section.setDescription(request.getDescription());
        section.setCapacity(request.getCapacity());
        section.setHasNumberedSeats(request.getHasNumberedSeats());

        section = sectionRepository.save(section);
        log.info("Section created: {} for venue: {}", section.getName(), venue.getName());
        return toSectionResponse(section);
    }

    public List<SectionResponse> getSectionsByVenue(Long venueId) {
        return sectionRepository.findByVenueId(venueId).stream()
                .map(this::toSectionResponse)
                .collect(Collectors.toList());
    }

    // Seat methods
    public SeatResponse createSeat(CreateSeatRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.getVenueId()));

        Seat seat = new Seat();
        seat.setVenue(venue);
        seat.setRowNumber(request.getRowNumber());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType() != null ? request.getSeatType() : "REGULAR");

        if (request.getSectionId() != null) {
            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + request.getSectionId()));
            seat.setSection(section);
        }

        seat = seatRepository.save(seat);
        log.info("Seat created: {} at venue: {}", seat.getSeatNumber(), venue.getName());
        return toSeatResponse(seat, true);
    }

    public List<SeatResponse> getSeatsByVenue(Long venueId) {
        return seatRepository.findByVenueId(venueId).stream()
                .map(s -> toSeatResponse(s, true))
                .collect(Collectors.toList());
    }

    public List<SeatResponse> getAvailableSeats(Long eventScheduleId) {
        // Get all reserved seat IDs for this schedule
        List<Long> reservedSeatIds = seatReservationRepository
                .findByEventScheduleIdAndStatus(eventScheduleId, "HOLDING")
                .stream()
                .map(r -> r.getSeat().getId())
                .collect(Collectors.toList());

        List<Long> confirmedSeatIds = seatReservationRepository
                .findByEventScheduleIdAndStatus(eventScheduleId, "CONFIRMED")
                .stream()
                .map(r -> r.getSeat().getId())
                .collect(Collectors.toList());

        reservedSeatIds.addAll(confirmedSeatIds);

        // We need the schedule to find the venue
        // For simplicity, get all reservations and find venue from there
        List<SeatReservation> reservations = seatReservationRepository.findByEventScheduleId(eventScheduleId);
        if (reservations.isEmpty()) {
            return List.of();
        }

        Long venueId = reservations.get(0).getSeat().getVenue().getId();
        List<Seat> allSeats = seatRepository.findByVenueId(venueId);

        return allSeats.stream()
                .map(seat -> toSeatResponse(seat, !reservedSeatIds.contains(seat.getId())))
                .collect(Collectors.toList());
    }

    private SectionResponse toSectionResponse(Section section) {
        return new SectionResponse(
                section.getId(), section.getVenue().getId(),
                section.getName(), section.getDescription(),
                section.getCapacity(), section.getHasNumberedSeats());
    }

    private SeatResponse toSeatResponse(Seat seat, boolean available) {
        return new SeatResponse(
                seat.getId(), seat.getVenue().getId(),
                seat.getSection() != null ? seat.getSection().getId() : null,
                seat.getSection() != null ? seat.getSection().getName() : null,
                seat.getRowNumber(), seat.getSeatNumber(),
                seat.getSeatType(), available);
    }
}
