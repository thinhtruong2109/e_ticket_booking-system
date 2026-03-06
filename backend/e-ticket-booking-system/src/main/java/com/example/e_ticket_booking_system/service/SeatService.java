package com.example.e_ticket_booking_system.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.BulkCreateSeatsRequest;
import com.example.e_ticket_booking_system.dto.request.CreateSeatRequest;
import com.example.e_ticket_booking_system.dto.request.CreateSectionRequest;
import com.example.e_ticket_booking_system.dto.response.SeatResponse;
import com.example.e_ticket_booking_system.dto.response.SectionResponse;
import com.example.e_ticket_booking_system.entity.EventSchedule;
import com.example.e_ticket_booking_system.entity.Seat;
import com.example.e_ticket_booking_system.entity.SeatReservation;
import com.example.e_ticket_booking_system.entity.Section;
import com.example.e_ticket_booking_system.entity.Venue;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.EventScheduleRepository;
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
    private final EventScheduleRepository scheduleRepository;

    // Section methods
    public SectionResponse createSection(CreateSectionRequest request) {
        Optional<Venue> optionalVenue = venueRepository.findById(request.getVenueId());
        if (!optionalVenue.isPresent()) {
            throw new ResourceNotFoundException("Venue not found with id: " + request.getVenueId());
        }
        Venue venue = optionalVenue.get();

        // Validate: duplicate section name within venue
        Section existingSection = sectionRepository.findByVenueIdAndName(request.getVenueId(), request.getName());
        if (existingSection != null) {
            throw new BadRequestException("Section with name '" + request.getName()
                    + "' already exists in this venue");
        }

        // Validate: sum of section capacities must not exceed venue totalCapacity
        if (request.getCapacity() != null && venue.getTotalCapacity() != null) {
            int currentTotalCapacity = sectionRepository.sumCapacityByVenueId(request.getVenueId());
            int newTotal = currentTotalCapacity + request.getCapacity();
            if (newTotal > venue.getTotalCapacity()) {
                throw new BadRequestException(
                        "Total section capacity (" + newTotal + ") would exceed venue total capacity ("
                                + venue.getTotalCapacity() + "). Current sections use "
                                + currentTotalCapacity + " capacity");
            }
        }

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

    public SectionResponse updateSection(Long id, CreateSectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));

        if (request.getName() != null) {
            // Check duplicate name (excluding self)
            Section existing = sectionRepository.findByVenueIdAndName(section.getVenue().getId(), request.getName());
            if (existing != null && !existing.getId().equals(id)) {
                throw new BadRequestException("Section with name '" + request.getName()
                        + "' already exists in this venue");
            }
            section.setName(request.getName());
        }
        if (request.getCapacity() != null) {
            // Validate capacity against venue total
            Venue venue = section.getVenue();
            if (venue.getTotalCapacity() != null) {
                int currentTotal = sectionRepository.sumCapacityByVenueId(venue.getId());
                Integer reqCap = request.getCapacity();
                Integer secCap = section.getCapacity();
                int newCap = reqCap != null ? reqCap : 0;
                int oldCap = secCap != null ? secCap : 0;
                int diff = newCap - oldCap;
                if (currentTotal + diff > venue.getTotalCapacity()) {
                    throw new BadRequestException(
                            "Total section capacity would exceed venue total capacity (" + venue.getTotalCapacity() + ")");
                }
            }
            section.setCapacity(request.getCapacity());
        }
        if (request.getDescription() != null) section.setDescription(request.getDescription());
        if (request.getHasNumberedSeats() != null) section.setHasNumberedSeats(request.getHasNumberedSeats());

        section = sectionRepository.save(section);
        log.info("Section updated: {}", section.getName());
        return toSectionResponse(section);
    }

    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + id));

        // Delete all seats in this section first
        List<Seat> seats = seatRepository.findBySectionId(id);
        if (!seats.isEmpty()) {
            seatRepository.deleteAll(seats);
            log.info("Deleted {} seats from section: {}", seats.size(), section.getName());
        }

        sectionRepository.delete(section);
        log.info("Section deleted: {}", section.getName());
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

            // Validate: seat count must not exceed section capacity
            if (section.getCapacity() != null) {
                long currentSeatCount = seatRepository.countBySectionId(section.getId());
                if (currentSeatCount + 1 > section.getCapacity()) {
                    throw new BadRequestException(
                            "Adding this seat would exceed section '" + section.getName()
                                    + "' capacity (" + section.getCapacity() + "). Current seats: "
                                    + currentSeatCount);
                }
            }

            seat.setSection(section);
        }

        seat = seatRepository.save(seat);
        log.info("Seat created: {} at venue: {}", seat.getSeatNumber(), venue.getName());
        return toSeatResponse(seat, true);
    }

    public List<SeatResponse> bulkCreateSeats(BulkCreateSeatsRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.getVenueId()));

        Section section = null;
        if (request.getSectionId() != null) {
            section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Section not found with id: " + request.getSectionId()));
        }

        String defaultSeatType = request.getSeatType() != null ? request.getSeatType() : "REGULAR";

        // Calculate total new seats to create
        int totalNewSeats = 0;
        List<Seat> seatsToSave = new ArrayList<>();
        for (BulkCreateSeatsRequest.RowSpec row : request.getRows()) {
            if (row.getEndNumber() < row.getStartNumber()) {
                throw new IllegalArgumentException(
                        "End number must be >= start number for row: " + row.getRowLabel());
            }
            totalNewSeats += (row.getEndNumber() - row.getStartNumber() + 1);
        }

        // Validate: total seats must not exceed section capacity
        if (section != null && section.getCapacity() != null) {
            long currentSeatCount = seatRepository.countBySectionId(section.getId());
            if (currentSeatCount + totalNewSeats > section.getCapacity()) {
                throw new BadRequestException(
                        "Adding " + totalNewSeats + " seats would exceed section '"
                                + section.getName() + "' capacity (" + section.getCapacity()
                                + "). Current seats: " + currentSeatCount);
            }
        }

        for (BulkCreateSeatsRequest.RowSpec row : request.getRows()) {
            String rowSeatType = row.getSeatType() != null ? row.getSeatType() : defaultSeatType;

            for (int i = row.getStartNumber(); i <= row.getEndNumber(); i++) {
                Seat seat = new Seat();
                seat.setVenue(venue);
                seat.setSection(section);
                seat.setRowNumber(row.getRowLabel());
                seat.setSeatNumber(String.valueOf(i));
                seat.setSeatType(rowSeatType);
                seatsToSave.add(seat);
            }
        }

        List<Seat> savedSeats = seatRepository.saveAll(seatsToSave);
        log.info("Bulk created {} seats at venue: {}", savedSeats.size(), venue.getName());

        List<SeatResponse> responseList = new ArrayList<>();
        for (Seat s : savedSeats) {
            responseList.add(toSeatResponse(s, true));
        }
        return responseList;
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

    // Release seat reservations that have expired holdExpiresAt but still HOLDING
    public void releaseExpiredReservations() {
        List<SeatReservation> expired = seatReservationRepository
                .findByStatusAndHoldExpiresAtBefore("HOLDING", LocalDateTime.now());
        for (SeatReservation r : expired) {
            r.setStatus("RELEASED");
            seatReservationRepository.save(r);
            log.info("Released expired seat reservation: seat {} for schedule {}",
                    r.getSeat().getSeatNumber(), r.getEventSchedule().getId());
        }
    }

    public List<SeatResponse> getAvailableSeats(Long eventScheduleId) {
        // Lấy schedule để tìm venue qua event
        EventSchedule schedule = scheduleRepository.findById(eventScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + eventScheduleId));

        Long venueId = schedule.getEvent().getVenue().getId();
        List<Seat> allSeats = seatRepository.findByVenueId(venueId);

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
