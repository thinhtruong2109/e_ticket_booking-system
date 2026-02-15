package com.example.e_ticket_booking_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        Optional<Venue> optionalVenue = venueRepository.findById(request.getVenueId());
        if (!optionalVenue.isPresent()) {
            throw new ResourceNotFoundException("Venue not found with id: " + request.getVenueId());
        }
        Venue venue = optionalVenue.get();

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
        List<Section> sections = sectionRepository.findByVenueId(venueId);
        List<SectionResponse> responseList = new ArrayList<>();
        for (Section section : sections) {
            SectionResponse response = toSectionResponse(section);
            responseList.add(response);
        }
        return responseList;
    }

    // Seat methods
    public SeatResponse createSeat(CreateSeatRequest request) {
        Optional<Venue> optionalVenue = venueRepository.findById(request.getVenueId());
        if (!optionalVenue.isPresent()) {
            throw new ResourceNotFoundException("Venue not found with id: " + request.getVenueId());
        }
        Venue venue = optionalVenue.get();

        Seat seat = new Seat();
        seat.setVenue(venue);
        seat.setRowNumber(request.getRowNumber());
        seat.setSeatNumber(request.getSeatNumber());

        // Xác định loại ghế: mặc định là REGULAR
        if (request.getSeatType() != null) {
            seat.setSeatType(request.getSeatType());
        } else {
            seat.setSeatType("REGULAR");
        }

        if (request.getSectionId() != null) {
            Optional<Section> optionalSection = sectionRepository.findById(request.getSectionId());
            if (!optionalSection.isPresent()) {
                throw new ResourceNotFoundException("Section not found with id: " + request.getSectionId());
            }
            Section section = optionalSection.get();
            seat.setSection(section);
        }

        seat = seatRepository.save(seat);
        log.info("Seat created: {} at venue: {}", seat.getSeatNumber(), venue.getName());
        return toSeatResponse(seat, true);
    }

    public List<SeatResponse> getSeatsByVenue(Long venueId) {
        List<Seat> seats = seatRepository.findByVenueId(venueId);
        List<SeatResponse> responseList = new ArrayList<>();
        for (Seat s : seats) {
            SeatResponse response = toSeatResponse(s, true);
            responseList.add(response);
        }
        return responseList;
    }

    public List<SeatResponse> getAvailableSeats(Long eventScheduleId) {
        // Lấy danh sách các seat ID đã bị đặt (HOLDING và CONFIRMED)
        List<SeatReservation> holdingReservations = seatReservationRepository
                .findByEventScheduleIdAndStatus(eventScheduleId, "HOLDING");
        List<SeatReservation> confirmedReservations = seatReservationRepository
                .findByEventScheduleIdAndStatus(eventScheduleId, "CONFIRMED");

        // Thu thập tất cả seat ID đã bị đặt vào một danh sách
        List<Long> reservedSeatIds = new ArrayList<>();
        for (SeatReservation r : holdingReservations) {
            reservedSeatIds.add(r.getSeat().getId());
        }
        for (SeatReservation r : confirmedReservations) {
            reservedSeatIds.add(r.getSeat().getId());
        }

        // Tìm venue từ các reservation
        List<SeatReservation> reservations = seatReservationRepository.findByEventScheduleId(eventScheduleId);
        if (reservations.isEmpty()) {
            return new ArrayList<>();
        }

        Long venueId = reservations.get(0).getSeat().getVenue().getId();
        List<Seat> allSeats = seatRepository.findByVenueId(venueId);

        // Kiểm tra từng seat có available không
        List<SeatResponse> responseList = new ArrayList<>();
        for (Seat seat : allSeats) {
            boolean isReserved = reservedSeatIds.contains(seat.getId());
            boolean isAvailable = !isReserved;
            SeatResponse response = toSeatResponse(seat, isAvailable);
            responseList.add(response);
        }
        return responseList;
    }

    private SectionResponse toSectionResponse(Section section) {
        return new SectionResponse(
                section.getId(), section.getVenue().getId(),
                section.getName(), section.getDescription(),
                section.getCapacity(), section.getHasNumberedSeats());
    }

    private SeatResponse toSeatResponse(Seat seat, boolean available) {
        // Lấy sectionId và sectionName nếu có
        Long sectionId = null;
        String sectionName = null;
        if (seat.getSection() != null) {
            sectionId = seat.getSection().getId();
            sectionName = seat.getSection().getName();
        }

        return new SeatResponse(
                seat.getId(), seat.getVenue().getId(),
                sectionId, sectionName,
                seat.getRowNumber(), seat.getSeatNumber(),
                seat.getSeatType(), available);
    }
}
