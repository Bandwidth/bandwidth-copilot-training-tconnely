package com.coveros.training.flavorhub.service;

import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing recipes
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }
    
    public Optional<Recipe> getRecipeById(Long id) {
        return recipeRepository.findById(id);
    }
    
    public List<Recipe> getRecipesByDifficulty(String difficultyLevel) {
        return recipeRepository.findByDifficultyLevel(difficultyLevel);
    }
    
    public List<Recipe> getRecipesByCuisine(String cuisineType) {
        return recipeRepository.findByCuisineType(cuisineType);
    }
    
    public List<Recipe> searchRecipes(String searchTerm) {
        return recipeRepository.findByNameContainingIgnoreCase(searchTerm);
    }
    
    public Recipe saveRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }
    
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }
    
    /**
     * Add a rating to a recipe and update the average rating
     * @param recipeId The ID of the recipe to rate
     * @param rating The rating value (1-5)
     * @return The updated recipe with new rating
     * @throws IllegalArgumentException if rating is not between 1 and 5
     * @throws java.util.NoSuchElementException if recipe is not found
     */
    @Transactional
    public Recipe addRating(Long recipeId, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Recipe not found"));
        
        // Calculate new average rating
        Double oldRating = recipe.getAverageRating();
        Integer oldCount = recipe.getRatingCount();
        Double newAverage = ((oldRating * oldCount) + rating) / (oldCount + 1);
        
        recipe.setAverageRating(newAverage);
        recipe.setRatingCount(oldCount + 1);
        
        return recipeRepository.save(recipe);
    }
    
    /**
     * Find recipes that can be made based on available ingredients in the pantry
     * NOTE: This method is intentionally left incomplete for workshop participants
     * Participants will use GitHub Copilot to implement this recommendation logic
     */
    // TODO: Implement method to recommend recipes based on pantry ingredients
    
    /**
     * Get recipes that match specific dietary requirements or filters
     * NOTE: This is a more advanced feature to be implemented during the workshop
     */
    // TODO: Implement advanced filtering logic
}
