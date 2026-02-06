package com.example.demo.service;

import com.example.demo.dto.allergen.AllergenResponseDTO;
import com.example.demo.entity.Allergen;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AllergenRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllergenService {

    private final AllergenRepository allergenRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AllergenResponseDTO> getAllAllergens() {
        log.info("Fetching all allergens");
        return allergenRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AllergenResponseDTO> getUserAllergens(Long userId) {
        log.info("Fetching allergens for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return user.getAllergens().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AllergenResponseDTO> updateUserAllergens(Long userId, List<String> allergenNames) {
        log.info("Updating allergens for user: {} with names: {}", userId, allergenNames);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Clear existing allergens
        user.getAllergens().clear();

        // Add new allergens
        if (allergenNames != null && !allergenNames.isEmpty()) {
            Set<Allergen> newAllergens = new HashSet<>();

            for (String name : allergenNames) {
                Allergen allergen = allergenRepository.findByName(name)
                        .orElseGet(() -> {
                            // Create new allergen if it doesn't exist
                            Allergen newAllergen = Allergen.builder()
                                    .name(name)
                                    .build();
                            return allergenRepository.save(newAllergen);
                        });
                newAllergens.add(allergen);
            }

            user.getAllergens().addAll(newAllergens);
        }

        User savedUser = userRepository.save(user);
        log.info("Updated allergens for user: {}", userId);

        return savedUser.getAllergens().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AllergenResponseDTO mapToResponse(Allergen allergen) {
        return AllergenResponseDTO.builder()
                .id(allergen.getId())
                .name(allergen.getName())
                .severityLevel(allergen.getSeverityLevel())
                .build();
    }
}
