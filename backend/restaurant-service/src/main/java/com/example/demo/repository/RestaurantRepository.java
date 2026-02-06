package com.example.demo.repository;

import com.example.demo.entity.Restaurant;
import com.example.demo.entity.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByStatus(RestaurantStatus status);

    List<Restaurant> findByOwnerId(Long ownerId);

    List<Restaurant> findByIsVerified(Boolean isVerified);

    List<Restaurant> findByStatusAndIsVerified(RestaurantStatus status, Boolean isVerified);

    Long countByStatus(RestaurantStatus status);

    Long countByIsVerified(Boolean isVerified);
}