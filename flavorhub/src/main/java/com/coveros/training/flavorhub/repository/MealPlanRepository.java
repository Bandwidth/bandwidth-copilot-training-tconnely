package com.coveros.training.flavorhub.repository;

import com.coveros.training.flavorhub.model.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing MealPlan entities
 */
@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    
    /**
     * Find all meal plans for a specific user
     */
    List<MealPlan> findByUserId(Long userId);
    
    /**
     * Find meal plans for a user within a date range
     */
    List<MealPlan> findByUserIdAndStartDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find current meal plans for a user (where today is between start and end date)
     */
    List<MealPlan> findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        Long userId, LocalDate startDate, LocalDate endDate
    );
    
    /**
     * Find a meal plan by ID and user ID to ensure ownership
     */
    Optional<MealPlan> findByIdAndUserId(Long id, Long userId);
}
