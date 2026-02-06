package com.example.demo.repository;

import com.example.demo.entity.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllergenRepository extends JpaRepository<Allergen,Long> {
    Optional<Allergen> findByName(String name);
}
