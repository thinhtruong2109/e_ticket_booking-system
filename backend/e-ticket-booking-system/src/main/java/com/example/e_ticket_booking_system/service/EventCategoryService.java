package com.example.e_ticket_booking_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.CreateEventCategoryRequest;
import com.example.e_ticket_booking_system.dto.response.EventCategoryResponse;
import com.example.e_ticket_booking_system.entity.EventCategory;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.EventCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventCategoryService {

    private static final Logger log = LoggerFactory.getLogger(EventCategoryService.class);

    private final EventCategoryRepository categoryRepository;

    public List<EventCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EventCategoryResponse getCategoryById(Long id) {
        EventCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return toResponse(category);
    }

    public EventCategoryResponse createCategory(CreateEventCategoryRequest request) {
        if (categoryRepository.findByName(request.getName()) != null) {
            throw new BadRequestException("Category name already exists: " + request.getName());
        }

        EventCategory category = new EventCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIconUrl(request.getIconUrl());

        category = categoryRepository.save(category);
        log.info("Category created: {}", category.getName());
        return toResponse(category);
    }

    public EventCategoryResponse updateCategory(Long id, CreateEventCategoryRequest request) {
        EventCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (request.getName() != null) {
            EventCategory existing = categoryRepository.findByName(request.getName());
            if (existing != null && !existing.getId().equals(id)) {
                throw new BadRequestException("Category name already exists: " + request.getName());
            }
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getIconUrl() != null) {
            category.setIconUrl(request.getIconUrl());
        }

        category = categoryRepository.save(category);
        log.info("Category updated: {}", category.getName());
        return toResponse(category);
    }

    public void deleteCategory(Long id) {
        EventCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
        log.info("Category deleted: {}", category.getName());
    }

    private EventCategoryResponse toResponse(EventCategory category) {
        return new EventCategoryResponse(
                category.getId(), category.getName(), category.getDescription(),
                category.getIconUrl(), category.getCreatedAt());
    }
}
