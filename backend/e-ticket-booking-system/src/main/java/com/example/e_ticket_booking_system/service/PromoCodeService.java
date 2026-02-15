package com.example.e_ticket_booking_system.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.CreatePromoCodeRequest;
import com.example.e_ticket_booking_system.dto.response.PromoCodeResponse;
import com.example.e_ticket_booking_system.entity.PromoCode;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.PromocodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private static final Logger log = LoggerFactory.getLogger(PromoCodeService.class);

    private final PromocodeRepository promoCodeRepository;

    public PromoCodeResponse createPromoCode(CreatePromoCodeRequest request) {
        if (promoCodeRepository.findByCode(request.getCode()) != null) {
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

        promo = promoCodeRepository.save(promo);
        log.info("Promo code created: {}", promo.getCode());
        return toResponse(promo);
    }

    public List<PromoCodeResponse> getAllPromoCodes() {
        return promoCodeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PromoCodeResponse> getActivePromoCodes() {
        return promoCodeRepository.findByStatus("ACTIVE").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PromoCodeResponse getPromoCodeById(Long id) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found"));
        return toResponse(promo);
    }

    public PromoCodeResponse deactivatePromoCode(Long id) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found"));
        promo.setStatus("DISABLED");
        promoCodeRepository.save(promo);
        log.info("Promo code deactivated: {}", promo.getCode());
        return toResponse(promo);
    }

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

    private PromoCodeResponse toResponse(PromoCode promo) {
        return new PromoCodeResponse(
                promo.getId(), promo.getCode(), promo.getDescription(),
                promo.getDiscountType(), promo.getDiscountValue(),
                promo.getMinOrderAmount(), promo.getMaxDiscountAmount(),
                promo.getUsageLimit(), promo.getUsedCount(),
                promo.getValidFrom(), promo.getValidTo(),
                promo.getStatus(), promo.getCreatedAt());
    }
}
