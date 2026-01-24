package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dishes")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_available")
    private Boolean isAvailable;

    private Double price;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "base_protein_g")
    private Double baseProteinG;

    @Column(name = "base_fat_g")
    private Double baseFatG;

    @Column(name = "base_carbs_g")
    private Double baseCarbsG;

    @Column(name = "base_kcal")
    private Double baseKcal;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL)
    private List<DishComponent> dishComponents = new ArrayList<>();

}