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
     * Get the recipe of the day
     * Returns a deterministic recipe based on the current date
     */
    @GetMapping("/daily")
    public ResponseEntity<Recipe> getDailyRecipe() {
        return recipeService.getDailyRecipe()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get recipes by difficulty level
     * Returns all recipes matching the specified difficulty level
     * 
     * @param level the difficulty level (Easy, Medium, Hard)
     * @return list of recipes with the specified difficulty
     */
    @GetMapping("/difficulty/{level}")
    public ResponseEntity<List<Recipe>> getRecipesByDifficulty(@PathVariable String level) {
        return ResponseEntity.ok(recipeService.getRecipesByDifficulty(level));
    }
    
    /**
     * Get recipes by cuisine type
     * Returns all recipes matching the specified cuisine type
     * 
     * @param type the cuisine type (Italian, Mexican, Asian, etc.)
     * @return list of recipes with the specified cuisine
     */
    @GetMapping("/cuisine/{type}")
    public ResponseEntity<List<Recipe>> getRecipesByCuisine(@PathVariable String type) {
        return ResponseEntity.ok(recipeService.getRecipesByCuisine(type));
    }
    
    /**
     * Recommend recipes based on available pantry ingredients
     * Scores recipes by how many ingredients the user already has
     * Returns recipes sorted by match count (most ingredients available first)
     * 
     * @param userId the user ID to get recommendations for
     * @return list of recipes with matching ingredients, sorted by match count
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<Recipe>> getRecommendedRecipes(@RequestParam Long userId) {
        return ResponseEntity.ok(recipeService.recommendRecipesByPantryIngredients(userId));
    }
    
    /**
     * Get recipes that can be made COMPLETELY with available pantry ingredients
     * All ingredients required by the recipe must be in the user's pantry
     * Much more restrictive than /recommended endpoint
     * 
     * @param userId the user ID to get recipes for
     * @return list of recipes that can be made completely
     */
    @GetMapping("/makeable")
    public ResponseEntity<List<Recipe>> getMakeableRecipes(@RequestParam Long userId) {
        return ResponseEntity.ok(recipeService.findCompleteRecipesByPantry(userId));
    }
    
    /**
     * Get recipes that are missing only a few ingredients
     * Useful for suggesting recipes the user is close to being able to make
     * 
     * @param userId the user ID to get recipes for
     * @param maxMissing the maximum number of missing ingredients allowed (default 2)
     * @return list of recipes missing at most maxMissing ingredients
     */
    @GetMapping("/almost-makeable")
    public ResponseEntity<List<Recipe>> getAlmostMakeableRecipes(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "2") int maxMissing) {
        return ResponseEntity.ok(recipeService.findRecipesMissingFewIngredients(userId, maxMissing));
    }
    
    /**
     * Advanced filtering endpoint combining multiple criteria
     * All specified filters are combined with AND logic (recipe must match ALL criteria)
     * 
     * @param userId optional user ID for pantry ingredient matching
     * @param difficulty optional difficulty level (Easy, Medium, Hard)
     * @param cuisineType optional cuisine type (Italian, Mexican, Asian, etc.)
     * @param maxCookingTime optional maximum total cooking time in minutes
     * @param minIngredientMatch optional minimum ingredient match percentage (0-100)
     * @return list of recipes matching all specified criteria
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Recipe>> filterRecipes(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) Integer maxCookingTime,
            @RequestParam(required = false) Integer minIngredientMatch) {
        return ResponseEntity.ok(recipeService.getRecipesByAdvancedFilter(
            userId, difficulty, cuisineType, maxCookingTime, minIngredientMatch));
    }
    
    /**
     * Convenience endpoint for quick recipes
     * Returns recipes that can be completed within a specified time limit
     * 
     * @param maxMinutes the maximum total cooking time in minutes
     * @return list of recipes that fit within the time budget
     */
    @GetMapping("/quick")
    public ResponseEntity<List<Recipe>> getQuickRecipes(@RequestParam Integer maxMinutes) {
        return ResponseEntity.ok(recipeService.getQuickRecipes(maxMinutes));
    }

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
