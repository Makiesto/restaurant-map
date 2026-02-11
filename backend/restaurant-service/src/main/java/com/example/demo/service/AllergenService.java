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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllergenService {

    private final AllergenRepository allergenRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AllergenResponseDTO> getAllAllergens() {
        return allergenRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Allergen getAllergenById(Long id) {
        return allergenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allergen not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Allergen getAllergenByName(String name) {
        return allergenRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Allergen not found with name: " + name));
    }

    @Transactional(readOnly = true)
    public List<AllergenResponseDTO> getUserAllergens(Long userId) {
        log.info("Getting allergens for user ID: {}", userId);

        // Use a custom query to eagerly fetch allergens
        User user = userRepository.findByIdWithAllergens(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Now we can safely iterate because allergens are already loaded
        return user.getAllergens().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AllergenResponseDTO> updateUserAllergens(Long userId, List<String> allergenNames) {
        log.info("Updating allergens for user ID: {} with allergens: {}", userId, allergenNames);

        // Use JOIN FETCH to avoid lazy loading issues
        User user = userRepository.findByIdWithAllergens(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Clear existing allergens
        user.getAllergens().clear();

        // Add new allergens
        if (allergenNames != null && !allergenNames.isEmpty()) {
            List<Allergen> allergens = allergenNames.stream()
                    .map(name -> allergenRepository.findByName(name)
                            .orElseThrow(() -> new ResourceNotFoundException("Allergen not found: " + name)))
                    .collect(Collectors.toList());

            user.getAllergens().addAll(allergens);
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully updated allergens for user ID: {}", userId);

        // Return the updated allergens as DTOs
        return savedUser.getAllergens().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AllergenResponseDTO mapToDTO(Allergen allergen) {
        return AllergenResponseDTO.builder()
                .id(allergen.getId())
                .name(allergen.getName())
                .build();
    }
}