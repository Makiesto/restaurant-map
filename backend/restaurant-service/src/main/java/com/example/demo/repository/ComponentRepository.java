package com.example.demo.repository;

import com.example.demo.entity.Component;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {

    @Query("SELECT c FROM Component c LEFT JOIN FETCH c.allergens WHERE c.id IN :ids")
    List<Component> findAllWithAllergensByIdIn(@Param("ids") List<Long> ids);

}
