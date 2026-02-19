package com.coveros.training.flavorhub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a recipe with its ingredients and instructions
 */
@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Recipe name is required")
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "prep_time")
    @Min(value = 0, message = "Prep time must be positive")
    private Integer prepTime; // in minutes
    
    @Column(name = "cook_time")
    @Min(value = 0, message = "Cook time must be positive")
    private Integer cookTime; // in minutes
    
    @Column(name = "servings")
    @Min(value = 1, message = "Servings must be at least 1")
    private Integer servings;
    
    @Column(name = "difficulty_level")
    private String difficultyLevel; // e.g., "Easy", "Medium", "Hard"
    
    @Column(name = "cuisine_type")
    private String cuisineType; // e.g., "Italian", "Mexican", "Asian"
    
    @ElementCollection
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "recipe_instructions", joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "step_number")
    private List<String> instructions = new ArrayList<>();
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "average_rating")
    private Double averageRating = 0.0;
    
    @Column(name = "rating_count")
    private Integer ratingCount = 0;
    
    public Recipe(String name, String description, Integer prepTime, Integer cookTime, 
                  Integer servings, String difficultyLevel, String cuisineType) {
        this.name = name;
        this.description = description;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.servings = servings;
        this.difficultyLevel = difficultyLevel;
        this.cuisineType = cuisineType;
        this.averageRating = 0.0;
        this.ratingCount = 0;
    }
}
