package com.coveros.training.flavorhub.controller;

import com.coveros.training.flavorhub.dto.RatingRequest;
import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST Controller for managing recipes
 */
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {
    
    private final RecipeService recipeService;
    
    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Search recipes by name
     * NOTE: This endpoint is complete and working
     */
    @GetMapping("/search")
    public ResponseEntity<List<Recipe>> searchRecipes(@RequestParam String query) {
        return ResponseEntity.ok(recipeService.searchRecipes(query));
    }
    
    /**
     * Get recipes by difficulty level
     * NOTE: Workshop participants will implement this endpoint using Copilot
     */
    // TODO: Implement GET /api/recipes/difficulty/{level} endpoint
    
    /**
     * Get recipes by cuisine type
     * NOTE: Workshop participants will implement this endpoint using Copilot
     */
    // TODO: Implement GET /api/recipes/cuisine/{type} endpoint
    
    /**
     * Recommend recipes based on available pantry ingredients
     * NOTE: This is an advanced endpoint to be implemented during the workshop
     */
    // TODO: Implement GET /api/recipes/recommendations endpoint
    
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@Valid @RequestBody Recipe recipe) {
        Recipe saved = recipeService.saveRecipe(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable Long id, 
            @Valid @RequestBody Recipe recipe) {
        return recipeService.getRecipeById(id)
                .map(existing -> {
                    recipe.setId(id);
                    return ResponseEntity.ok(recipeService.saveRecipe(recipe));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Add a rating to a recipe
     * @param id The recipe ID
     * @param ratingRequest The rating request containing the rating value (1-5)
     * @return Updated recipe with new rating
     */
    @PutMapping("/{id}/rate")
    public ResponseEntity<Recipe> rateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest ratingRequest) {
        try {
            Recipe updatedRecipe = recipeService.addRating(id, ratingRequest.getRating());
            return ResponseEntity.ok(updatedRecipe);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
