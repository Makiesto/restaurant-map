package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToMany(mappedBy = "allergens")
    @Builder.Default
    private Set<Component> components = new HashSet<>();

    @ManyToMany(mappedBy = "allergens")
    @Builder.Default
    private Set<User> users = new HashSet<>();
}