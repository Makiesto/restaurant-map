package com.example.demo.service;

import com.example.demo.dto.allergen.AllergenResponseDTO;
import com.example.demo.entity.Allergen;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AllergenRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllergenServiceTest {

    @Mock
    private AllergenRepository allergenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AllergenService allergenService;

    private Allergen glutenAllergen;
    private Allergen nutsAllergen;
    private User testUser;

    @BeforeEach
    void setUp() {
        glutenAllergen = Allergen.builder()
                .id(1L)
                .name("Gluten")
                .severityLevel("High")
                .build();

        nutsAllergen = Allergen.builder()
                .id(2L)
                .name("Nuts")
                .severityLevel("High")
                .build();

        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .allergens(new HashSet<>())
                .build();
    }

    @Test
    void getAllAllergens_ShouldReturnAllAllergens() {
        // Given
        when(allergenRepository.findAll()).thenReturn(List.of(glutenAllergen, nutsAllergen));

        // When
        List<AllergenResponseDTO> results = allergenService.getAllAllergens();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(AllergenResponseDTO::getName)
                .containsExactly("Gluten", "Nuts");
    }

    @Test
    void getAllergenById_WhenExists_ShouldReturnAllergen() {
        // Given
        when(allergenRepository.findById(1L)).thenReturn(Optional.of(glutenAllergen));

        // When
        Allergen result = allergenService.getAllergenById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Gluten");
    }

    @Test
    void getAllergenById_WhenNotExists_ShouldThrowException() {
        // Given
        when(allergenRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getAllergenById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allergen not found with ID: 999");
    }

    @Test
    void getAllergenByName_WhenExists_ShouldReturnAllergen() {
        // Given
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));

        // When
        Allergen result = allergenService.getAllergenByName("Gluten");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Gluten");
    }

    @Test
    void getAllergenByName_WhenNotExists_ShouldThrowException() {
        // Given
        when(allergenRepository.findByName("Unknown")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getAllergenByName("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allergen not found with name: Unknown");
    }

    @Test
    void getUserAllergens_ShouldReturnUserAllergens() {
        // Given
        testUser.getAllergens().add(glutenAllergen);
        testUser.getAllergens().add(nutsAllergen);
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));

        // When
        List<AllergenResponseDTO> results = allergenService.getUserAllergens(1L);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(AllergenResponseDTO::getName)
                .containsExactlyInAnyOrder("Gluten", "Nuts");
    }

    @Test
    void getUserAllergens_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByIdWithAllergens(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getUserAllergens(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");
    }

    @Test
    void updateUserAllergens_WithNewAllergens_ShouldUpdateSuccessfully() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));
        when(allergenRepository.findByName("Nuts")).thenReturn(Optional.of(nutsAllergen));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> allergenNames = List.of("Gluten", "Nuts");

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, allergenNames);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(AllergenResponseDTO::getName)
                .containsExactlyInAnyOrder("Gluten", "Nuts");

        verify(userRepository).save(argThat(user ->
                user.getAllergens().size() == 2
        ));
    }

    @Test
    void updateUserAllergens_WithEmptyList_ShouldClearAllergens() {
        // Given
        testUser.getAllergens().add(glutenAllergen);
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, List.of());

        // Then
        assertThat(results).isEmpty();

        verify(userRepository).save(argThat(user ->
                user.getAllergens().isEmpty()
        ));
    }

    @Test
    void updateUserAllergens_WithNullList_ShouldClearAllergens() {
        // Given
        testUser.getAllergens().add(glutenAllergen);
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, null);

        // Then
        assertThat(results).isEmpty();

        verify(userRepository).save(argThat(user ->
                user.getAllergens().isEmpty()
        ));
    }

    @Test
    void updateUserAllergens_WithInvalidAllergen_ShouldThrowException() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("InvalidAllergen")).thenReturn(Optional.empty());

        List<String> allergenNames = List.of("InvalidAllergen");

        // When/Then
        assertThatThrownBy(() -> allergenService.updateUserAllergens(1L, allergenNames))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allergen not found: InvalidAllergen");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserAllergens_ShouldReplaceExistingAllergens() {
        // Given
        testUser.getAllergens().add(glutenAllergen);
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Nuts")).thenReturn(Optional.of(nutsAllergen));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> newAllergenNames = List.of("Nuts");

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, newAllergenNames);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Nuts");

        verify(userRepository).save(argThat(user -> {
            Set<Allergen> allergens = user.getAllergens();
            return allergens.size() == 1 &&
                   allergens.stream().anyMatch(a -> a.getName().equals("Nuts"));
        }));
    }
}