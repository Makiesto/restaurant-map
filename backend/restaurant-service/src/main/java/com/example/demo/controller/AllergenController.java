package com.example.demo.controller;

import com.example.demo.dto.allergen.AllergenResponseDTO;
import com.example.demo.dto.allergen.UpdateUserAllergensRequestDTO;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.AllergenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AllergenController {

    private final AllergenService allergenService;
    private final SecurityUtil securityUtil;

    /**
     * PUBLIC: Get all available allergens
     */
    @GetMapping("/allergens")
    public ResponseEntity<List<AllergenResponseDTO>> getAllAllergens() {
        log.info("GET /api/allergens - fetching all allergens");
        List<AllergenResponseDTO> allergens = allergenService.getAllAllergens();
        return ResponseEntity.ok(allergens);
    }

    /**
     * AUTHENTICATED: Get current user's allergen preferences
     */
    @GetMapping("/users/me/allergens")
    public ResponseEntity<List<AllergenResponseDTO>> getUserAllergens() {
        Long userId = securityUtil.getCurrentUserId();
        log.info("GET /api/users/me/allergens - fetching allergens for user: {}", userId);

        List<AllergenResponseDTO> allergens = allergenService.getUserAllergens(userId);
        return ResponseEntity.ok(allergens);
    }

    /**
     * AUTHENTICATED: Update current user's allergen preferences
     */
    @PutMapping("/users/me/allergens")
    public ResponseEntity<List<AllergenResponseDTO>> updateUserAllergens(
            @Valid @RequestBody UpdateUserAllergensRequestDTO request) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("PUT /api/users/me/allergens - updating allergens for user: {}", userId);

        List<AllergenResponseDTO> allergens = allergenService.updateUserAllergens(userId, request.getAllergenNames());
        return ResponseEntity.ok(allergens);
    }
}