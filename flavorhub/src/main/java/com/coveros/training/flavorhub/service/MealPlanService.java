package com.coveros.training.flavorhub.service;

import com.coveros.training.flavorhub.model.MealPlan;
import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.repository.MealPlanRepository;
import com.coveros.training.flavorhub.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing user meal plans
 * Provides functionality to create, update, delete, and query meal plans
 * Handles adding/removing recipes from meal plans
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MealPlanService {
    
    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;
    
    /**
     * Create a new meal plan for a user
     * 
     * @param mealPlan the meal plan to create
     * @return the created meal plan
     */
    public MealPlan createMealPlan(MealPlan mealPlan) {
        log.info("Creating new meal plan: {} for user: {}", mealPlan.getPlanName(), mealPlan.getUserId());
        return mealPlanRepository.save(mealPlan);
    }
    
    /**
     * Get a specific meal plan by ID for a user
     * Verifies ownership to ensure users can only access their own meal plans
     * 
     * @param id the meal plan ID
     * @param userId the user ID requesting the plan
     * @return Optional containing the meal plan if found and owned by the user
     */
    public Optional<MealPlan> getMealPlanById(Long id, Long userId) {
        log.debug("Fetching meal plan {} for user {}", id, userId);
        return mealPlanRepository.findByIdAndUserId(id, userId);
    }
    
    /**
     * Get all meal plans for a specific user
     * 
     * @param userId the user ID
     * @return list of meal plans for the user
     */
    public List<MealPlan> getUserMealPlans(Long userId) {
        log.debug("Fetching all meal plans for user {}", userId);
        return mealPlanRepository.findByUserId(userId);
    }
    
    /**
     * Get meal plans for a user within a specific date range
     * 
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of meal plans within the date range
     */
    public List<MealPlan> getMealPlansByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching meal plans for user {} between {} and {}", userId, startDate, endDate);
        return mealPlanRepository.findByUserIdAndStartDateBetween(userId, startDate, endDate);
    }
    
    /**
     * Get current active meal plans for a user (where today falls within the plan date range)
     * 
     * @param userId the user ID
     * @return list of currently active meal plans
     */
    public List<MealPlan> getCurrentMealPlans(Long userId) {
        LocalDate today = LocalDate.now();
        log.debug("Fetching active meal plans for user {} on {}", userId, today);
        return mealPlanRepository.findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            userId, today, today
        );
    }
    
    /**
     * Update an existing meal plan
     * Verifies ownership before updating
     * 
     * @param id the meal plan ID to update
     * @param userId the user ID requesting the update
     * @param updatedPlan the updated meal plan data
     * @return Optional containing the updated meal plan if found and updated
     */
    public Optional<MealPlan> updateMealPlan(Long id, Long userId, MealPlan updatedPlan) {
        return mealPlanRepository.findByIdAndUserId(id, userId)
            .map(existingPlan -> {
                log.info("Updating meal plan {} for user {}", id, userId);
                existingPlan.setPlanName(updatedPlan.getPlanName());
                existingPlan.setDescription(updatedPlan.getDescription());
                existingPlan.setStartDate(updatedPlan.getStartDate());
                existingPlan.setEndDate(updatedPlan.getEndDate());
                return mealPlanRepository.save(existingPlan);
            });
    }
    
    /**
     * Delete a meal plan
     * Verifies ownership before deletion
     * 
     * @param id the meal plan ID to delete
     * @param userId the user ID requesting the deletion
     * @return true if the meal plan was deleted, false if not found or not owned by user
     */
    public boolean deleteMealPlan(Long id, Long userId) {
        Optional<MealPlan> mealPlan = mealPlanRepository.findByIdAndUserId(id, userId);
        if (mealPlan.isPresent()) {
            log.info("Deleting meal plan {} for user {}", id, userId);
            mealPlanRepository.delete(mealPlan.get());
            return true;
        }
        log.warn("Attempted to delete non-existent or unauthorized meal plan {} for user {}", id, userId);
        return false;
    }
    
    /**
     * Add a recipe to a meal plan
     * Verifies ownership and recipe existence
     * 
     * @param mealPlanId the meal plan ID
     * @param recipeId the recipe ID to add
     * @param userId the user ID requesting the operation
     * @return Optional containing the updated meal plan if successful
     */
    public Optional<MealPlan> addRecipeToMealPlan(Long mealPlanId, Long recipeId, Long userId) {
        Optional<MealPlan> mealPlan = mealPlanRepository.findByIdAndUserId(mealPlanId, userId);
        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        
        if (mealPlan.isPresent() && recipe.isPresent()) {
            MealPlan plan = mealPlan.get();
            Recipe rec = recipe.get();
            
            if (!plan.getRecipes().contains(rec)) {
                plan.getRecipes().add(rec);
                log.info("Added recipe {} to meal plan {} for user {}", recipeId, mealPlanId, userId);
                return Optional.of(mealPlanRepository.save(plan));
            } else {
                log.debug("Recipe {} already in meal plan {}", recipeId, mealPlanId);
                return mealPlan;
            }
        }
        
        log.warn("Failed to add recipe {} to meal plan {}. Plan exists: {}, Recipe exists: {}",
            recipeId, mealPlanId, mealPlan.isPresent(), recipe.isPresent());
        return Optional.empty();
    }
    
    /**
     * Remove a recipe from a meal plan
     * Verifies ownership
     * 
     * @param mealPlanId the meal plan ID
     * @param recipeId the recipe ID to remove
     * @param userId the user ID requesting the operation
     * @return Optional containing the updated meal plan if successful
     */
    public Optional<MealPlan> removeRecipeFromMealPlan(Long mealPlanId, Long recipeId, Long userId) {
        Optional<MealPlan> mealPlan = mealPlanRepository.findByIdAndUserId(mealPlanId, userId);
        
        if (mealPlan.isPresent()) {
            MealPlan plan = mealPlan.get();
            boolean removed = plan.getRecipes().removeIf(recipe -> recipe.getId().equals(recipeId));
            
            if (removed) {
                log.info("Removed recipe {} from meal plan {} for user {}", recipeId, mealPlanId, userId);
                return Optional.of(mealPlanRepository.save(plan));
            } else {
                log.debug("Recipe {} not found in meal plan {}", recipeId, mealPlanId);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Get all recipes in a meal plan
     * Verifies ownership
     * 
     * @param mealPlanId the meal plan ID
     * @param userId the user ID requesting the operation
     * @return list of recipes in the meal plan
     */
    public List<Recipe> getMealPlanRecipes(Long mealPlanId, Long userId) {
        return mealPlanRepository.findByIdAndUserId(mealPlanId, userId)
            .map(MealPlan::getRecipes)
            .orElseGet(List::of);
    }
    
    /**
     * Calculate total cooking time for all recipes in a meal plan
     * 
     * @param mealPlanId the meal plan ID
     * @param userId the user ID requesting the operation
     * @return total cooking time in minutes
     */
    public int calculateTotalCookingTime(Long mealPlanId, Long userId) {
        return mealPlanRepository.findByIdAndUserId(mealPlanId, userId)
            .map(plan -> plan.getRecipes().stream()
                .mapToInt(recipe -> (recipe.getPrepTime() != null ? recipe.getPrepTime() : 0) +
                                    (recipe.getCookTime() != null ? recipe.getCookTime() : 0))
                .sum())
            .orElse(0);
    }
}
