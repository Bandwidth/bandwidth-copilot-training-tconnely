package com.coveros.training.flavorhub.service;

import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.repository.RecipeRepository;
import com.coveros.training.flavorhub.repository.UserPantryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing recipes
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    private final UserPantryRepository userPantryRepository;
    
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
     * Get the recipe of the day based on current date
     * Uses a deterministic algorithm: the recipe index is calculated from the day of year
     * This ensures the same recipe is returned for all requests on the same day
     * 
     * @return Optional containing the daily recipe, or empty if no recipes exist
     */
    public Optional<Recipe> getDailyRecipe() {
        try {
            List<Recipe> allRecipes = recipeRepository.findAll();
            
            if (allRecipes.isEmpty()) {
                log.warn("No recipes available for daily recipe selection");
                return Optional.empty();
            }
            
            // Use day of year as seed for deterministic selection
            // This ensures the same recipe is selected throughout the day
            LocalDate today = LocalDate.now();
            int dayOfYear = today.getDayOfYear();
            int recipeIndex = dayOfYear % allRecipes.size();
            
            Recipe dailyRecipe = allRecipes.get(recipeIndex);
            log.info("Selected daily recipe: {} for date: {}", dailyRecipe.getName(), today);
            
            return Optional.of(dailyRecipe);
            
        } catch (Exception e) {
            log.error("Error selecting daily recipe", e);
            return Optional.empty();
        }
    }
    
    /**
     * Find recipes that can be made based on available ingredients in the pantry
     * Scores each recipe based on how many of its ingredients the user has available
     * Returns recipes sorted by ingredient match count in descending order
     * 
     * @param userId the user ID to get pantry ingredients for
     * @return list of recipes sorted by ingredient match count (most ingredients first)
     */
    public List<Recipe> recommendRecipesByPantryIngredients(Long userId) {
        try {
            // Get all ingredient names in user's pantry (case-insensitive)
            Set<String> pantryIngredientNames = userPantryRepository.findByUserId(userId)
                .stream()
                .map(pantryItem -> pantryItem.getIngredient().getName().toLowerCase())
                .collect(Collectors.toSet());
            
            log.info("User {} has {} ingredients in pantry", userId, pantryIngredientNames.size());
            
            if (pantryIngredientNames.isEmpty()) {
                log.debug("User {} has no pantry ingredients, returning all recipes", userId);
                return recipeRepository.findAll();
            }
            
            // Get all recipes and score them based on ingredient matches
            List<Recipe> allRecipes = recipeRepository.findAll();
            
            List<Recipe> recommendedRecipes = allRecipes.stream()
                .map(recipe -> {
                    // Count how many recipe ingredients match pantry ingredients (case-insensitive)
                    long matchingIngredients = recipe.getIngredients().stream()
                        .filter(recipeIngredient -> pantryIngredientNames.contains(
                            recipeIngredient.getIngredientName().toLowerCase()
                        ))
                        .count();
                    
                    // Create a simple wrapper to track the score
                    return new RecipeWithScore(recipe, matchingIngredients);
                })
                // Filter to recipes with at least 1 matching ingredient
                .filter(recipeScore -> recipeScore.matchingIngredients > 0)
                // Sort by matching ingredients count in descending order
                .sorted((a, b) -> Long.compare(b.matchingIngredients, a.matchingIngredients))
                // Extract the recipe
                .map(recipeScore -> recipeScore.recipe)
                .collect(Collectors.toList());
            
            log.info("Found {} recommended recipes for user {} based on pantry items",
                recommendedRecipes.size(), userId);
            
            return recommendedRecipes;
            
        } catch (Exception e) {
            log.error("Error recommending recipes based on pantry ingredients for user {}", userId, e);
            // Return all recipes as fallback
            return recipeRepository.findAll();
        }
    }
    
    /**
     * Find recipes that can be made COMPLETELY (all ingredients available) with pantry items
     * More restrictive than recommendRecipesByPantryIngredients
     * 
     * @param userId the user ID to get pantry ingredients for
     * @return list of recipes where ALL ingredients are available in the pantry
     */
    public List<Recipe> findCompleteRecipesByPantry(Long userId) {
        try {
            // Get all ingredient names in user's pantry (case-insensitive)
            Set<String> pantryIngredientNames = userPantryRepository.findByUserId(userId)
                .stream()
                .map(pantryItem -> pantryItem.getIngredient().getName().toLowerCase())
                .collect(Collectors.toSet());
            
            if (pantryIngredientNames.isEmpty()) {
                log.debug("User {} has no pantry ingredients", userId);
                return List.of();
            }
            
            // Get all recipes and filter those where ALL ingredients are in pantry
            List<Recipe> completeRecipes = recipeRepository.findAll().stream()
                .filter(recipe -> {
                    // Check if ALL recipe ingredients are in user's pantry (case-insensitive)
                    return recipe.getIngredients().stream()
                        .allMatch(recipeIngredient -> pantryIngredientNames.contains(
                            recipeIngredient.getIngredientName().toLowerCase()
                        ));
                })
                .collect(Collectors.toList());
            
            log.info("Found {} recipes that can be made completely with user {}'s pantry items",
                completeRecipes.size(), userId);
            
            return completeRecipes;
            
        } catch (Exception e) {
            log.error("Error finding complete recipes by pantry for user {}", userId, e);
            return List.of();
        }
    }
    
    /**
     * Get recipes missing only a few ingredients (within a threshold)
     * Useful for suggesting recipes the user is close to being able to make
     * 
     * @param userId the user ID to get pantry ingredients for
     * @param maxMissingIngredients the maximum number of missing ingredients allowed
     * @return list of recipes missing at most maxMissingIngredients ingredients
     */
    public List<Recipe> findRecipesMissingFewIngredients(Long userId, int maxMissingIngredients) {
        try {
            // Get all ingredient names in user's pantry (case-insensitive)
            Set<String> pantryIngredientNames = userPantryRepository.findByUserId(userId)
                .stream()
                .map(pantryItem -> pantryItem.getIngredient().getName().toLowerCase())
                .collect(Collectors.toSet());
            
            if (pantryIngredientNames.isEmpty()) {
                return List.of();
            }
            
            // Find recipes with only a few missing ingredients
            List<Recipe> closeRecipes = recipeRepository.findAll().stream()
                .filter(recipe -> {
                    long missingIngredients = recipe.getIngredients().stream()
                        .filter(recipeIngredient -> !pantryIngredientNames.contains(
                            recipeIngredient.getIngredientName().toLowerCase()
                        ))
                        .count();
                    
                    return missingIngredients <= maxMissingIngredients && missingIngredients > 0;
                })
                .collect(Collectors.toList());
            
            log.info("Found {} recipes missing at most {} ingredients for user {}",
                closeRecipes.size(), maxMissingIngredients, userId);
            
            return closeRecipes;
            
        } catch (Exception e) {
            log.error("Error finding recipes missing few ingredients for user {}", userId, e);
            return List.of();
        }
    }
    
    /**
     * Simple wrapper class to track recipe with ingredient match score
     */
    private static class RecipeWithScore {
        Recipe recipe;
        long matchingIngredients;
        
        RecipeWithScore(Recipe recipe, long matchingIngredients) {
            this.recipe = recipe;
            this.matchingIngredients = matchingIngredients;
        }
    }
    
    /**
     * Get recipes that match specific dietary requirements and filters
     * This advanced filtering method allows combining multiple filter criteria:
     * - Difficulty level (Easy, Medium, Hard)
     * - Cuisine type (Italian, Mexican, Asian, etc.)
     * - Maximum cooking time (in minutes)
     * - Minimum ingredient match from pantry (0-100%, optional)
     * 
     * Filters are combined with AND logic - a recipe must match ALL specified criteria
     * 
     * @param userId the user ID for pantry ingredient matching (can be null to skip ingredient matching)
     * @param difficulty the difficulty level to filter by (can be null to include all difficulties)
     * @param cuisineType the cuisine type to filter by (can be null to include all cuisines)
     * @param maxCookingTime the maximum total cooking time in minutes (can be null for no limit)
     * @param minIngredientMatchPercentage minimum percentage of ingredients to match from pantry, 0-100 (optional)
     * @return list of recipes matching ALL specified filter criteria
     */
    public List<Recipe> getRecipesByAdvancedFilter(
            Long userId,
            String difficulty,
            String cuisineType,
            Integer maxCookingTime,
            Integer minIngredientMatchPercentage) {
        
        try {
            log.info("Applying advanced filters - difficulty: {}, cuisine: {}, maxCookingTime: {}, minMatch: {}",
                difficulty, cuisineType, maxCookingTime, minIngredientMatchPercentage);
            
            // Get all recipes as starting point
            List<Recipe> recipes = recipeRepository.findAll();
            
            // Prepare pantry ingredients if userId is provided
            Set<String> pantryIngredientNames = null;
            if (userId != null) {
                pantryIngredientNames = userPantryRepository.findByUserId(userId)
                    .stream()
                    .map(pantryItem -> pantryItem.getIngredient().getName().toLowerCase())
                    .collect(Collectors.toSet());
                log.debug("User {} has {} ingredients available", userId, pantryIngredientNames.size());
            }
            
            // Apply filters with AND logic
            final Set<String> finalPantryIngredientNames = pantryIngredientNames;
            final Integer finalMinMatchPercentage = minIngredientMatchPercentage != null ? 
                minIngredientMatchPercentage : 0;
            
            List<Recipe> filteredRecipes = recipes.stream()
                // Filter by difficulty
                .filter(recipe -> difficulty == null || 
                    (recipe.getDifficultyLevel() != null && 
                     recipe.getDifficultyLevel().equalsIgnoreCase(difficulty)))
                // Filter by cuisine
                .filter(recipe -> cuisineType == null || 
                    (recipe.getCuisineType() != null && 
                     recipe.getCuisineType().equalsIgnoreCase(cuisineType)))
                // Filter by maximum cooking time
                .filter(recipe -> maxCookingTime == null || 
                    ((recipe.getPrepTime() != null ? recipe.getPrepTime() : 0) +
                     (recipe.getCookTime() != null ? recipe.getCookTime() : 0)) <= maxCookingTime)
                // Filter by ingredient match percentage
                .filter(recipe -> {
                    if (finalPantryIngredientNames == null || finalPantryIngredientNames.isEmpty()) {
                        return finalMinMatchPercentage == 0; // Allow if no ingredient matching required
                    }
                    
                    // Calculate percentage of recipe ingredients in pantry
                    long ingredientCount = recipe.getIngredients().size();
                    if (ingredientCount == 0) {
                        return true; // Allow recipes with no ingredients listed
                    }
                    
                    long matchingCount = recipe.getIngredients().stream()
                        .filter(recipeIngredient -> finalPantryIngredientNames.contains(
                            recipeIngredient.getIngredientName().toLowerCase()
                        ))
                        .count();
                    
                    int matchPercentage = (int) ((matchingCount * 100) / ingredientCount);
                    return matchPercentage >= finalMinMatchPercentage;
                })
                .collect(Collectors.toList());
            
            log.info("Advanced filter returned {} recipes matching criteria", filteredRecipes.size());
            return filteredRecipes;
            
        } catch (Exception e) {
            log.error("Error applying advanced filters", e);
            return List.of();
        }
    }
    
    /**
     * Convenience method for filtering by difficulty and cuisine only
     * 
     * @param difficulty the difficulty level (Easy, Medium, Hard)
     * @param cuisineType the cuisine type (Italian, Mexican, Asian, etc.)
     * @return list of recipes matching both criteria
     */
    public List<Recipe> getRecipesByDifficultyAndCuisine(String difficulty, String cuisineType) {
        return getRecipesByAdvancedFilter(null, difficulty, cuisineType, null, null);
    }
    
    /**
     * Convenience method for filtering by cooking time
     * Useful for finding quick recipes within a specific time budget
     * 
     * @param maxCookingTime the maximum total cooking time in minutes (prep + cook time)
     * @return list of recipes that can be completed within the time limit
     */
    public List<Recipe> getQuickRecipes(Integer maxCookingTime) {
        return getRecipesByAdvancedFilter(null, null, null, maxCookingTime, null);
    }
}
