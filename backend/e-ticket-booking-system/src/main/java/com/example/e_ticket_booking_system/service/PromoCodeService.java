package com.example.e_ticket_booking_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.request.AvailablePromoRequest;
import com.example.e_ticket_booking_system.dto.request.BookingItemRequest;
import com.example.e_ticket_booking_system.dto.request.CreatePromoCodeRequest;
import com.example.e_ticket_booking_system.dto.response.AvailablePromoResponse;
import com.example.e_ticket_booking_system.dto.response.PromoCodeResponse;
import com.example.e_ticket_booking_system.dto.response.PromoPreview;
import com.example.e_ticket_booking_system.entity.Event;
import com.example.e_ticket_booking_system.entity.PromoCode;
import com.example.e_ticket_booking_system.entity.PromoCodeEventJoin;
import com.example.e_ticket_booking_system.entity.TicketType;
import com.example.e_ticket_booking_system.entity.User;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.EventRepository;
import com.example.e_ticket_booking_system.repository.PromoCodeEventJoinRepository;
import com.example.e_ticket_booking_system.repository.PromocodeRepository;
import com.example.e_ticket_booking_system.repository.TicketTypeRepository;
import com.example.e_ticket_booking_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private static final Logger log = LoggerFactory.getLogger(PromoCodeService.class);

    private final PromocodeRepository promoCodeRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PromoCodeEventJoinRepository promoCodeEventRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    // ======================= ADMIN: Create GLOBAL promo code =======================

    @Transactional
    public PromoCodeResponse createPromoCodeByAdmin(CreatePromoCodeRequest request) {
        Long adminId = securityUtils.getCurrentUserId();
        Optional<User> optionalAdmin = userRepository.findById(adminId);
        if (!optionalAdmin.isPresent()) {
            throw new ResourceNotFoundException("Admin user not found");
        }
        User admin = optionalAdmin.get();

        // Admin chỉ được tạo GLOBAL
        if (!"GLOBAL".equals(request.getApplicationType())) {
            throw new BadRequestException("Admin can only create GLOBAL promo codes");
        }

        return createPromoCodeInternal(request, admin);
    }

    // ======================= ORGANIZER: CRUD promo codes =======================

    @Transactional
    public PromoCodeResponse createPromoCodeByOrganizer(CreatePromoCodeRequest request) {
        Long organizerId = securityUtils.getCurrentUserId();
        Optional<User> optionalOrganizer = userRepository.findById(organizerId);
        if (!optionalOrganizer.isPresent()) {
            throw new ResourceNotFoundException("Organizer user not found");
        }
        User organizer = optionalOrganizer.get();

        // Organizer chỉ được tạo ORGANIZER_ALL hoặc SPECIFIC_EVENTS
        if ("GLOBAL".equals(request.getApplicationType())) {
            throw new BadRequestException("Organizer cannot create GLOBAL promo codes. Use ORGANIZER_ALL or SPECIFIC_EVENTS");
        }
        if (!"ORGANIZER_ALL".equals(request.getApplicationType()) &&
            !"SPECIFIC_EVENTS".equals(request.getApplicationType())) {
            throw new BadRequestException("Invalid application type. Must be ORGANIZER_ALL or SPECIFIC_EVENTS");
        }

        // Nếu SPECIFIC_EVENTS, validate eventIds
        if ("SPECIFIC_EVENTS".equals(request.getApplicationType())) {
            if (request.getEventIds() == null || request.getEventIds().isEmpty()) {
                throw new BadRequestException("Event IDs are required for SPECIFIC_EVENTS application type");
            }
            // Kiểm tra tất cả events thuộc về organizer này
            for (Long eventId : request.getEventIds()) {
                Optional<Event> optionalEvent = eventRepository.findById(eventId);
                if (!optionalEvent.isPresent()) {
                    throw new ResourceNotFoundException("Event not found: " + eventId);
                }
                Event event = optionalEvent.get();
                if (!event.getOrganizer().getId().equals(organizerId)) {
                    throw new ForbiddenException("Event " + eventId + " does not belong to you");
                }
            }
        }

        return createPromoCodeInternal(request, organizer);
    }

    /** Organizer lấy tất cả promo code của mình */
    public List<PromoCodeResponse> getPromoCodesByOrganizer() {
        Long organizerId = securityUtils.getCurrentUserId();
        List<PromoCode> promos = promoCodeRepository.findByCreatedById(organizerId);
        List<PromoCodeResponse> responseList = new ArrayList<>();
        for (PromoCode promo : promos) {
            responseList.add(toResponse(promo));
        }
        return responseList;
    }

    /** Organizer lấy promo code theo ID (chỉ của mình) */
    public PromoCodeResponse getPromoCodeByIdForOrganizer(Long promoId) {
        Long organizerId = securityUtils.getCurrentUserId();
        Optional<PromoCode> optionalPromo = promoCodeRepository.findById(promoId);
        if (!optionalPromo.isPresent()) {
            throw new ResourceNotFoundException("Promo code not found");
        }
        PromoCode promo = optionalPromo.get();
        if (!promo.getCreatedBy().getId().equals(organizerId)) {
            throw new ForbiddenException("This promo code does not belong to you");
        }
        return toResponse(promo);
    }

    /** Organizer cập nhật promo code của mình */
    @Transactional
    public PromoCodeResponse updatePromoCodeByOrganizer(Long promoId, CreatePromoCodeRequest request) {
        Long organizerId = securityUtils.getCurrentUserId();
        Optional<PromoCode> optionalPromo = promoCodeRepository.findById(promoId);
        if (!optionalPromo.isPresent()) {
            throw new ResourceNotFoundException("Promo code not found");
        }
        PromoCode promo = optionalPromo.get();
        if (!promo.getCreatedBy().getId().equals(organizerId)) {
            throw new ForbiddenException("This promo code does not belong to you");
        }

        // Không cho phép đổi sang GLOBAL
        if ("GLOBAL".equals(request.getApplicationType())) {
            throw new BadRequestException("Organizer cannot set application type to GLOBAL");
        }

        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new BadRequestException("Valid to must be after valid from");
        }

        // Check code uniqueness nếu đổi code
        if (!promo.getCode().equals(request.getCode().toUpperCase())) {
            if (promoCodeRepository.findByCode(request.getCode().toUpperCase()) != null) {
                throw new BadRequestException("Promo code already exists: " + request.getCode());
            }
        }

        promo.setCode(request.getCode().toUpperCase());
        promo.setDescription(request.getDescription());
        promo.setDiscountType(request.getDiscountType());
        promo.setDiscountValue(request.getDiscountValue());
        promo.setMinOrderAmount(request.getMinOrderAmount());
        promo.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promo.setUsageLimit(request.getUsageLimit());
        promo.setValidFrom(request.getValidFrom());
        promo.setValidTo(request.getValidTo());
        promo.setApplicationType(request.getApplicationType());

        promo = promoCodeRepository.save(promo);

        // Cập nhật event mappings
        promoCodeEventRepository.deleteByPromoCodeId(promoId);
        if ("SPECIFIC_EVENTS".equals(request.getApplicationType())) {
            if (request.getEventIds() == null || request.getEventIds().isEmpty()) {
                throw new BadRequestException("Event IDs are required for SPECIFIC_EVENTS application type");
            }
            for (Long eventId : request.getEventIds()) {
                Optional<Event> optionalEvent = eventRepository.findById(eventId);
                if (!optionalEvent.isPresent()) {
                    throw new ResourceNotFoundException("Event not found: " + eventId);
                }
                Event event = optionalEvent.get();
                if (!event.getOrganizer().getId().equals(organizerId)) {
                    throw new ForbiddenException("Event " + eventId + " does not belong to you");
                }
                PromoCodeEventJoin pce = new PromoCodeEventJoin();
                pce.setPromoCode(promo);
                pce.setEvent(event);
                promoCodeEventRepository.save(pce);
            }
        }

        log.info("Promo code updated by organizer {}: {}", organizerId, promo.getCode());
        return toResponse(promo);
    }

    /** Organizer deactivate promo code của mình */
    @Transactional
    public PromoCodeResponse deactivatePromoCodeByOrganizer(Long promoId) {
        Long organizerId = securityUtils.getCurrentUserId();
        Optional<PromoCode> optionalPromo = promoCodeRepository.findById(promoId);
        if (!optionalPromo.isPresent()) {
            throw new ResourceNotFoundException("Promo code not found");
        }
        PromoCode promo = optionalPromo.get();
        if (!promo.getCreatedBy().getId().equals(organizerId)) {
            throw new ForbiddenException("This promo code does not belong to you");
        }
        promo.setStatus("DISABLED");
        promoCodeRepository.save(promo);
        log.info("Promo code deactivated by organizer {}: {}", organizerId, promo.getCode());
        return toResponse(promo);
    }

    // ======================= ADMIN CRUD (giữ lại) =======================

    public List<PromoCodeResponse> getAllPromoCodes() {
        List<PromoCode> promos = promoCodeRepository.findAll();
        List<PromoCodeResponse> responseList = new ArrayList<>();
        for (PromoCode promo : promos) {
            responseList.add(toResponse(promo));
        }
        return responseList;
    }

    public List<PromoCodeResponse> getActivePromoCodes() {
        List<PromoCode> activePromos = promoCodeRepository.findByStatus("ACTIVE");
        List<PromoCodeResponse> responseList = new ArrayList<>();
        for (PromoCode promo : activePromos) {
            responseList.add(toResponse(promo));
        }
        return responseList;
    }

    public PromoCodeResponse getPromoCodeById(Long id) {
        Optional<PromoCode> optionalPromo = promoCodeRepository.findById(id);
        if (!optionalPromo.isPresent()) {
            throw new ResourceNotFoundException("Promo code not found");
        }
        PromoCode promo = optionalPromo.get();
        return toResponse(promo);
    }

    @Transactional
    public PromoCodeResponse updatePromoCodeByAdmin(Long promoId, CreatePromoCodeRequest request) {
        Optional<PromoCode> optionalPromo = promoCodeRepository.findById(promoId);
        if (!optionalPromo.isPresent()) {
            throw new ResourceNotFoundException("Promo code not found");
        }
        PromoCode promo = optionalPromo.get();

        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new BadRequestException("Valid to must be after valid from");
        }

        // Check code uniqueness nếu đổi code
        if (!promo.getCode().equals(request.getCode().toUpperCase())) {
            if (promoCodeRepository.findByCode(request.getCode().toUpperCase()) != null) {
                throw new BadRequestException("Promo code already exists: " + request.getCode());
            }
        }

        promo.setCode(request.getCode().toUpperCase());
        promo.setDescription(request.getDescription());
        promo.setDiscountType(request.getDiscountType());
        promo.setDiscountValue(request.getDiscountValue());
        promo.setMinOrderAmount(request.getMinOrderAmount());
        promo.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promo.setUsageLimit(request.getUsageLimit());
        promo.setValidFrom(request.getValidFrom());
        promo.setValidTo(request.getValidTo());
        promo.setApplicationType(request.getApplicationType());

        promo = promoCodeRepository.save(promo);

        // Cập nhật event mappings nếu SPECIFIC_EVENTS
        promoCodeEventRepository.deleteByPromoCodeId(promoId);
        if ("SPECIFIC_EVENTS".equals(request.getApplicationType())) {
            if (request.getEventIds() != null) {
                for (Long eventId : request.getEventIds()) {
                    Optional<Event> optionalEvent = eventRepository.findById(eventId);
                    if (!optionalEvent.isPresent()) {
                        throw new ResourceNotFoundException("Event not found: " + eventId);
                    }
                    Event event = optionalEvent.get();
                    PromoCodeEventJoin pce = new PromoCodeEventJoin();
                    pce.setPromoCode(promo);
                    pce.setEvent(event);
                    promoCodeEventRepository.save(pce);
                }
            }
        }

        log.info("Promo code updated by admin: {}", promo.getCode());
        return toResponse(promo);
    }

    @Transactional
    public PromoCodeResponse deactivatePromoCode(Long id) {
        Optional<PromoCode> optionalPromo = promoCodeRepository.findById(id);
        if (!optionalPromo.isPresent()) {
            throw new ResourceNotFoundException("Promo code not found");
        }
        PromoCode promo = optionalPromo.get();
        promo.setStatus("DISABLED");
        promoCodeRepository.save(promo);
        log.info("Promo code deactivated: {}", promo.getCode());
        return toResponse(promo);
    }

    // ======================= Expire & Available =======================

    public void expirePromoCodes() {
        List<PromoCode> activePromos = promoCodeRepository.findByStatus("ACTIVE");
        LocalDateTime now = LocalDateTime.now();
        for (PromoCode promo : activePromos) {
            if (promo.getValidTo().isBefore(now)) {
                promo.setStatus("EXPIRED");
                promoCodeRepository.save(promo);
                log.info("Promo code expired: {}", promo.getCode());
            }
        }
    }

    /**
     * Lấy danh sách promo code khả dụng cho một event cụ thể.
     * Bao gồm:
     * 1. GLOBAL promo codes (Admin tạo, áp dụng cho mọi event)
     * 2. ORGANIZER_ALL promo codes (Organizer tạo, áp dụng cho tất cả event của organizer đó)
     * 3. SPECIFIC_EVENTS promo codes (Organizer tạo, chỉ áp dụng cho event cụ thể)
     *
     * Promo code của Organizer khác tạo cho event khác sẽ KHÔNG hiển thị.
     */
    public AvailablePromoResponse getAvailablePromoCodes(AvailablePromoRequest request) {
        // 1. Tính totalAmount từ items
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long eventOrganizerId = null;

        for (BookingItemRequest item : request.getItems()) {
            Optional<TicketType> optionalTt = ticketTypeRepository.findById(item.getTicketTypeId());
            if (!optionalTt.isPresent()) {
                throw new ResourceNotFoundException("Ticket type not found: " + item.getTicketTypeId());
            }
            TicketType tt = optionalTt.get();
            if (!tt.getEvent().getId().equals(request.getEventId())) {
                throw new BadRequestException("Ticket type " + tt.getName() + " does not belong to this event");
            }
            if (eventOrganizerId == null) {
                eventOrganizerId = tt.getEvent().getOrganizer().getId();
            }
            BigDecimal subtotal = tt.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        // Fallback: lấy organizerId từ event nếu chưa có
        if (eventOrganizerId == null) {
            Optional<Event> optionalEvent = eventRepository.findById(request.getEventId());
            if (!optionalEvent.isPresent()) {
                throw new ResourceNotFoundException("Event not found");
            }
            Event event = optionalEvent.get();
            eventOrganizerId = event.getOrganizer().getId();
        }

        // 2. Query promo codes phù hợp cho event này (3 queries riêng biệt, merge kết quả)
        LocalDateTime now = LocalDateTime.now();
        List<PromoCode> availablePromos = new ArrayList<>();

        // 2a. GLOBAL: áp dụng cho tất cả events
        List<PromoCode> globalPromos = promoCodeRepository
                .findByApplicationTypeAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        "GLOBAL", "ACTIVE", now, now);
        availablePromos.addAll(globalPromos);

        // 2b. ORGANIZER_ALL: áp dụng cho tất cả events của organizer đó
        List<PromoCode> organizerPromos = promoCodeRepository
                .findByApplicationTypeAndCreatedByIdAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        "ORGANIZER_ALL", eventOrganizerId, "ACTIVE", now, now);
        availablePromos.addAll(organizerPromos);

        // 2c. SPECIFIC_EVENTS: lấy promoCodeIds từ bảng join, rồi query promo codes
        List<PromoCodeEventJoin> eventJoins = promoCodeEventRepository.findByEventId(request.getEventId());
        List<Long> specificPromoIds = new ArrayList<>();
        for (PromoCodeEventJoin join : eventJoins) {
            specificPromoIds.add(join.getPromoCode().getId());
        }
        if (!specificPromoIds.isEmpty()) {
            List<PromoCode> specificPromos = promoCodeRepository
                    .findByIdInAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                            specificPromoIds, "ACTIVE", now, now);
            availablePromos.addAll(specificPromos);
        }

        List<PromoPreview> previews = new ArrayList<>();
        for (PromoCode promo : availablePromos) {
            // Kiểm tra còn lượt
            if (promo.getUsageLimit() != null && promo.getUsedCount() >= promo.getUsageLimit()) {
                continue;
            }
            // Kiểm tra minOrderAmount
            if (promo.getMinOrderAmount() != null &&
                totalAmount.compareTo(promo.getMinOrderAmount()) < 0) {
                continue;
            }

            // 3. Tính discountAmount và finalAmount
            BigDecimal discount;
            if ("PERCENTAGE".equals(promo.getDiscountType())) {
                discount = totalAmount
                        .multiply(promo.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));
                if (promo.getMaxDiscountAmount() != null && discount.compareTo(promo.getMaxDiscountAmount()) > 0) {
                    discount = promo.getMaxDiscountAmount();
                }
            } else {
                discount = promo.getDiscountValue();
            }

            BigDecimal finalAmount = totalAmount.subtract(discount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            previews.add(new PromoPreview(
                    promo.getId(), promo.getCode(), promo.getDescription(),
                    promo.getApplicationType(), discount, finalAmount));
        }

        return new AvailablePromoResponse(totalAmount, previews);
    }

    // ======================= Internal helpers =======================

    private PromoCodeResponse createPromoCodeInternal(CreatePromoCodeRequest request, User creator) {
        if (promoCodeRepository.findByCode(request.getCode().toUpperCase()) != null) {
            throw new BadRequestException("Promo code already exists: " + request.getCode());
        }

        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new BadRequestException("Valid to must be after valid from");
        }

        PromoCode promo = new PromoCode();
        promo.setCode(request.getCode().toUpperCase());
        promo.setDescription(request.getDescription());
        promo.setDiscountType(request.getDiscountType());
        promo.setDiscountValue(request.getDiscountValue());
        promo.setMinOrderAmount(request.getMinOrderAmount());
        promo.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promo.setUsageLimit(request.getUsageLimit());
        promo.setUsedCount(0);
        promo.setValidFrom(request.getValidFrom());
        promo.setValidTo(request.getValidTo());
        promo.setStatus("ACTIVE");
        promo.setApplicationType(request.getApplicationType());
        promo.setCreatedBy(creator);

        promo = promoCodeRepository.save(promo);

        // Nếu SPECIFIC_EVENTS, tạo các event mappings
        if ("SPECIFIC_EVENTS".equals(request.getApplicationType()) &&
            request.getEventIds() != null && !request.getEventIds().isEmpty()) {
            for (Long eventId : request.getEventIds()) {
                Optional<Event> optionalEvent = eventRepository.findById(eventId);
                if (!optionalEvent.isPresent()) {
                    throw new ResourceNotFoundException("Event not found: " + eventId);
                }
                Event event = optionalEvent.get();
                PromoCodeEventJoin pce = new PromoCodeEventJoin();
                pce.setPromoCode(promo);
                pce.setEvent(event);
                promoCodeEventRepository.save(pce);
            }
        }

        log.info("Promo code created by {}: {}", creator.getEmail(), promo.getCode());
        return toResponse(promo);
    }

    private PromoCodeResponse toResponse(PromoCode promo) {
        PromoCodeResponse response = new PromoCodeResponse();
        response.setId(promo.getId());
        response.setCode(promo.getCode());
        response.setDescription(promo.getDescription());
        response.setDiscountType(promo.getDiscountType());
        response.setDiscountValue(promo.getDiscountValue());
        response.setMinOrderAmount(promo.getMinOrderAmount());
        response.setMaxDiscountAmount(promo.getMaxDiscountAmount());
        response.setUsageLimit(promo.getUsageLimit());
        response.setUsedCount(promo.getUsedCount());
        response.setValidFrom(promo.getValidFrom());
        response.setValidTo(promo.getValidTo());
        response.setStatus(promo.getStatus());
        response.setApplicationType(promo.getApplicationType());
        response.setCreatedByUserId(promo.getCreatedBy().getId());
        response.setCreatedByName(promo.getCreatedBy().getFullName());
        response.setCreatedAt(promo.getCreatedAt());

        // Lấy eventIds nếu là SPECIFIC_EVENTS
        if ("SPECIFIC_EVENTS".equals(promo.getApplicationType())) {
            List<PromoCodeEventJoin> joins = promoCodeEventRepository.findByPromoCodeId(promo.getId());
            List<Long> eventIds = new ArrayList<>();
            for (PromoCodeEventJoin join : joins) {
                eventIds.add(join.getEvent().getId());
            }
            response.setEventIds(eventIds);
        }

        return response;
    }
}
