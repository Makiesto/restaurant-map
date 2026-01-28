package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "components")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString(exclude = {"allergens", "dishComponents"})
@EqualsAndHashCode(exclude = {"allergens", "dishComponents"})
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "protein_per_100g", nullable = false)
    private Double proteinPer100g;

    @Column(name = "fat_per_100g", nullable = false)
    private Double fatPer100g;

    @Column(name = "carbs_per_100g", nullable = false)
    private Double carbsPer100g;

    @Column(name = "kcal_per_100g", nullable = false)
    private Double kcalPer100g;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "component_allergens",
        joinColumns = @JoinColumn(name = "component_id"),
        inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    @Builder.Default
    private Set<Allergen> allergens = new HashSet<>();

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DishComponent> dishComponents = new ArrayList<>();
}