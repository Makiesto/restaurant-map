package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "components")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "protein_per_100g")
    private Double proteinPer100g;

    @Column(name = "fat_per_100g")
    private Double fatPer100g;

    @Column(name = "carbs_per_100g")
    private Double carbsPer100g;

    @Column(name = "kcal_per_100g")
    private Double kcalPer100g;

    @Column(name = "is_allergen")
    private Boolean isAllergen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergen_id")
    private Allergen allergen;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL)
    private List<DishComponent> dishComponents = new ArrayList<>();

}