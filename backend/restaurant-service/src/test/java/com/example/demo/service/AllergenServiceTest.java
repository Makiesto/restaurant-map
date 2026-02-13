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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Additional comprehensive tests for AllergenService
 * Covers edge cases, concurrent modifications, bulk operations, and error scenarios
 */
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
    private Allergen dairyAllergen;
    private Allergen shellfishAllergen;
    private Allergen soyAllergen;
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

        dairyAllergen = Allergen.builder()
                .id(3L)
                .name("Dairy")
                .severityLevel("Medium")
                .build();

        shellfishAllergen = Allergen.builder()
                .id(4L)
                .name("Shellfish")
                .severityLevel("High")
                .build();

        soyAllergen = Allergen.builder()
                .id(5L)
                .name("Soy")
                .severityLevel("Low")
                .build();

        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .allergens(new HashSet<>())
                .build();
    }

    // ========== getAllAllergens Tests ==========

    @Test
    void getAllAllergens_WithLargeDataset_ShouldReturnAll() {
        // Given - Large dataset
        List<Allergen> allergens = Arrays.asList(
                glutenAllergen, nutsAllergen, dairyAllergen,
                shellfishAllergen, soyAllergen
        );
        when(allergenRepository.findAll()).thenReturn(allergens);

        // When
        List<AllergenResponseDTO> results = allergenService.getAllAllergens();

        // Then
        assertThat(results).hasSize(5);
        assertThat(results).extracting(AllergenResponseDTO::getName)
                .containsExactly("Gluten", "Nuts", "Dairy", "Shellfish", "Soy");
    }

    @Test
    void getAllAllergens_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(allergenRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<AllergenResponseDTO> results = allergenService.getAllAllergens();

        // Then
        assertThat(results).isEmpty();
        verify(allergenRepository).findAll();
    }

    @Test
    void getAllAllergens_CalledMultipleTimes_ShouldReturnConsistentResults() {
        // Given
        List<Allergen> allergens = Arrays.asList(glutenAllergen, nutsAllergen);
        when(allergenRepository.findAll()).thenReturn(allergens);

        // When - Call multiple times
        List<AllergenResponseDTO> results1 = allergenService.getAllAllergens();
        List<AllergenResponseDTO> results2 = allergenService.getAllAllergens();
        List<AllergenResponseDTO> results3 = allergenService.getAllAllergens();

        // Then
        assertThat(results1).hasSize(2);
        assertThat(results2).hasSize(2);
        assertThat(results3).hasSize(2);
        verify(allergenRepository, times(3)).findAll();
    }

    // ========== getAllergenById Tests ==========

    @Test
    void getAllergenById_WithValidId_ShouldReturnCorrectAllergen() {
        // Given
        when(allergenRepository.findById(1L)).thenReturn(Optional.of(glutenAllergen));

        // When
        Allergen result = allergenService.getAllergenById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Gluten");
        assertThat(result.getSeverityLevel()).isEqualTo("High");
    }

    @Test
    void getAllergenById_WithDifferentIds_ShouldReturnDifferentAllergens() {
        // Given
        when(allergenRepository.findById(1L)).thenReturn(Optional.of(glutenAllergen));
        when(allergenRepository.findById(2L)).thenReturn(Optional.of(nutsAllergen));

        // When
        Allergen result1 = allergenService.getAllergenById(1L);
        Allergen result2 = allergenService.getAllergenById(2L);

        // Then
        assertThat(result1.getName()).isEqualTo("Gluten");
        assertThat(result2.getName()).isEqualTo("Nuts");
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void getAllergenById_WithNegativeId_ShouldThrowException() {
        // Given
        when(allergenRepository.findById(-1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getAllergenById(-1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allergen not found with ID: -1");
    }

    @Test
    void getAllergenById_WithZeroId_ShouldThrowException() {
        // Given
        when(allergenRepository.findById(0L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getAllergenById(0L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== getAllergenByName Tests ==========

    @Test
    void getAllergenByName_WithExactCase_ShouldFindMatch() {
        // Given
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));

        // When
        Allergen result = allergenService.getAllergenByName("Gluten");

        // Then
        assertThat(result.getName()).isEqualTo("Gluten");
    }

    @Test
    void getAllergenByName_WithLowerCase_ShouldNotFind() {
        // Given - Case sensitivity matters
        when(allergenRepository.findByName("gluten")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getAllergenByName("gluten"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllergenByName_WithUpperCase_ShouldNotFind() {
        // Given - Case sensitivity matters
        when(allergenRepository.findByName("GLUTEN")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getAllergenByName("GLUTEN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllergenByName_WithWhitespace_ShouldNotFind() {
        // Given
        when(allergenRepository.findByName(" Gluten ")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getAllergenByName(" Gluten "))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllergenByName_WithEmptyString_ShouldThrowException() {
        // Given
        when(allergenRepository.findByName("")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getAllergenByName(""))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== getUserAllergens Tests ==========

    @Test
    void getUserAllergens_WithMultipleAllergens_ShouldReturnAll() {
        // Given
        testUser.getAllergens().add(glutenAllergen);
        testUser.getAllergens().add(nutsAllergen);
        testUser.getAllergens().add(dairyAllergen);
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));

        // When
        List<AllergenResponseDTO> results = allergenService.getUserAllergens(1L);

        // Then
        assertThat(results).hasSize(3);
        assertThat(results).extracting(AllergenResponseDTO::getName)
                .containsExactlyInAnyOrder("Gluten", "Nuts", "Dairy");
    }

    @Test
    void getUserAllergens_WithNoAllergens_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));

        // When
        List<AllergenResponseDTO> results = allergenService.getUserAllergens(1L);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void getUserAllergens_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findByIdWithAllergens(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.getUserAllergens(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");
    }

    @Test
    void getUserAllergens_CalledTwice_ShouldReturnSameResults() {
        // Given
        testUser.getAllergens().add(glutenAllergen);
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));

        // When
        List<AllergenResponseDTO> results1 = allergenService.getUserAllergens(1L);
        List<AllergenResponseDTO> results2 = allergenService.getUserAllergens(1L);

        // Then
        assertThat(results1).hasSize(1);
        assertThat(results2).hasSize(1);
        assertThat(results1.get(0).getName()).isEqualTo(results2.get(0).getName());
    }

    // ========== updateUserAllergens Tests ==========

    @Test
    void updateUserAllergens_AddingSingleAllergen_ShouldSucceed() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, List.of("Gluten"));

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Gluten");
        verify(userRepository).save(argThat(user -> user.getAllergens().size() == 1));
    }

    @Test
    void updateUserAllergens_AddingMultipleAllergens_ShouldSucceed() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));
        when(allergenRepository.findByName("Nuts")).thenReturn(Optional.of(nutsAllergen));
        when(allergenRepository.findByName("Dairy")).thenReturn(Optional.of(dairyAllergen));
        when(allergenRepository.findByName("Shellfish")).thenReturn(Optional.of(shellfishAllergen));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        List<String> allergenNames = Arrays.asList("Gluten", "Nuts", "Dairy", "Shellfish");

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, allergenNames);

        // Then
        assertThat(results).hasSize(4);
        verify(userRepository).save(argThat(user -> user.getAllergens().size() == 4));
    }

    @Test
    void updateUserAllergens_ReplacingExistingAllergens_ShouldClearAndAdd() {
        // Given
        testUser.getAllergens().add(glutenAllergen);
        testUser.getAllergens().add(nutsAllergen);
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Dairy")).thenReturn(Optional.of(dairyAllergen));
        when(allergenRepository.findByName("Soy")).thenReturn(Optional.of(soyAllergen));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(
                1L, Arrays.asList("Dairy", "Soy"));

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(AllergenResponseDTO::getName)
                .containsExactlyInAnyOrder("Dairy", "Soy");
        verify(userRepository).save(argThat(user -> {
            Set<Allergen> allergens = user.getAllergens();
            return allergens.size() == 2 &&
                   allergens.stream().noneMatch(a -> a.getName().equals("Gluten")) &&
                   allergens.stream().noneMatch(a -> a.getName().equals("Nuts"));
        }));
    }

    @Test
    void updateUserAllergens_WithDuplicateAllergenNames_ShouldOnlyAddOnce() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        List<String> duplicateNames = Arrays.asList("Gluten", "Gluten", "Gluten");

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, duplicateNames);

        // Then - Set naturally handles duplicates
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Gluten");
    }

    @Test
    void updateUserAllergens_WithMixOfValidAndInvalid_ShouldFailOnFirstInvalid() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));
        when(allergenRepository.findByName("InvalidAllergen")).thenReturn(Optional.empty());

        List<String> mixedNames = Arrays.asList("Gluten", "InvalidAllergen", "Nuts");

        // When/Then
        assertThatThrownBy(() -> allergenService.updateUserAllergens(1L, mixedNames))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allergen not found: InvalidAllergen");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserAllergens_ClearingAllAllergens_ShouldResultInEmpty() {
        // Given
        testUser.getAllergens().add(glutenAllergen);
        testUser.getAllergens().add(nutsAllergen);
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, Collections.emptyList());

        // Then
        assertThat(results).isEmpty();
        verify(userRepository).save(argThat(user -> user.getAllergens().isEmpty()));
    }

    @Test
    void updateUserAllergens_WithNullAllergensList_ShouldClearAllergens() {
        // Given
        testUser.getAllergens().add(glutenAllergen);
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, null);

        // Then
        assertThat(results).isEmpty();
        verify(userRepository).save(argThat(user -> user.getAllergens().isEmpty()));
    }

    @Test
    void updateUserAllergens_AddingAllergensTwice_ShouldReplaceNotAppend() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));
        when(allergenRepository.findByName("Nuts")).thenReturn(Optional.of(nutsAllergen));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When - First update
        allergenService.updateUserAllergens(1L, List.of("Gluten"));

        // Simulate user with updated allergens
        testUser.getAllergens().clear();
        testUser.getAllergens().add(glutenAllergen);

        // Second update
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, List.of("Nuts"));

        // Then - Should only have Nuts, not both
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Nuts");
    }

    @Test
    void updateUserAllergens_WithVeryLongList_ShouldHandleAll() {
        // Given
        List<Allergen> manyAllergens = new ArrayList<>();
        List<String> manyNames = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            Allergen allergen = Allergen.builder()
                    .id((long) i)
                    .name("Allergen" + i)
                    .build();
            manyAllergens.add(allergen);
            manyNames.add("Allergen" + i);
            when(allergenRepository.findByName("Allergen" + i)).thenReturn(Optional.of(allergen));
        }

        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, manyNames);

        // Then
        assertThat(results).hasSize(20);
        verify(allergenRepository, times(20)).findByName(anyString());
    }

    @Test
    void updateUserAllergens_ConcurrentModification_ShouldHandleGracefully() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));

        // Simulate concurrent modification during save
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            // Add an allergen during save to simulate concurrent modification
            user.getAllergens().add(nutsAllergen);
            return user;
        });

        // When
        List<AllergenResponseDTO> results = allergenService.updateUserAllergens(1L, List.of("Gluten"));

        // Then - Should still complete successfully
        assertThat(results).isNotEmpty();
    }

    @Test
    void updateUserAllergens_WithSpecialCharactersInName_ShouldNotFind() {
        // Given
        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Gluten@#$")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> allergenService.updateUserAllergens(1L, List.of("Gluten@#$")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUserAllergens_PreservesUserOtherFields_ShouldNotModifyThem() {
        // Given
        String originalEmail = "john@example.com";
        String originalFirstName = "John";

        when(userRepository.findByIdWithAllergens(1L)).thenReturn(Optional.of(testUser));
        when(allergenRepository.findByName("Gluten")).thenReturn(Optional.of(glutenAllergen));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        allergenService.updateUserAllergens(1L, List.of("Gluten"));

        // Then
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals(originalEmail) &&
                user.getFirstName().equals(originalFirstName)
        ));
    }
}