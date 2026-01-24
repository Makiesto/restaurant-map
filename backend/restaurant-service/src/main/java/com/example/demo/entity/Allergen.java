package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "allergens")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Allergen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "severity_level")
    private String severityLevel;

    @OneToMany(mappedBy = "allergen", cascade = CascadeType.ALL)
    private Set<UserAllergen> userAllergens = new HashSet<>();

    @OneToMany(mappedBy = "allergen", fetch = FetchType.LAZY)
    private Set<Component> components = new HashSet<>();
}