package com.coveros.training.flavorhub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's meal plan for a specific date or date range
 * Allows users to organize recipes they plan to cook
 */
@Entity
@Table(name = "meal_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @Column(name = "plan_name")
    private String planName;
    
    @Column(name = "start_date")
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "meal_plan_recipes",
        joinColumns = @JoinColumn(name = "meal_plan_id"),
        inverseJoinColumns = @JoinColumn(name = "recipe_id")
    )
    private List<Recipe> recipes = new ArrayList<>();
    
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}
