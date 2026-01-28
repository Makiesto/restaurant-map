package com.example.demo.repository;

import com.example.demo.entity.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergenRepository extends JpaRepository<Allergen,Long> {
}
