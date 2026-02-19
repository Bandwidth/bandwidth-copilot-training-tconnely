package com.coveros.training.flavorhub.service;

import com.coveros.training.flavorhub.model.Ingredient;
import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.model.RecipeIngredient;
import com.coveros.training.flavorhub.model.UserPantry;
import com.coveros.training.flavorhub.repository.RecipeRepository;
import com.coveros.training.flavorhub.repository.UserPantryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeService
 * Uses Mockito to mock dependencies and JUnit 5 for testing
 */
@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {
    
    @Mock
    private RecipeRepository recipeRepository;
    
    @Mock
    private UserPantryRepository userPantryRepository;
    
    @InjectMocks
    private RecipeService recipeService;
    
    private Recipe testRecipe;
    private Recipe testRecipe2;
    private Ingredient testIngredient;
    private UserPantry testPantryItem;
    
    @BeforeEach
    void setUp() {
        // Create test recipe 1
        testRecipe = new Recipe("Pasta", "Italian pasta dish", 10, 15, 4, "Easy", "Italian");
        testRecipe.setId(1L);
        testRecipe.setIngredients(Arrays.asList(
            new RecipeIngredient("Pasta", 400.0, "grams", null),
            new RecipeIngredient("Tomato", 200.0, "grams", "crushed"),
            new RecipeIngredient("Garlic", 3.0, "cloves", "minced")
        ));
        
        // Create test recipe 2
        testRecipe2 = new Recipe("Tacos", "Mexican street food", 5, 10, 2, "Easy", "Mexican");
        testRecipe2.setId(2L);
        testRecipe2.setIngredients(Arrays.asList(
            new RecipeIngredient("Tortilla", 4.0, "pieces", null),
            new RecipeIngredient("Beef", 200.0, "grams", "ground")
        ));
        
        // Create test ingredient
        testIngredient = new Ingredient();
        testIngredient.setId(1L);
        testIngredient.setName("Pasta");
        testIngredient.setCategory("Grains");
        
        // Create test pantry item
        testPantryItem = new UserPantry();
        testPantryItem.setId(1L);
        testPantryItem.setUserId(1L);
        testPantryItem.setIngredient(testIngredient);
        testPantryItem.setQuantity(500.0);
        testPantryItem.setUnit("grams");
    }
    
    // ========== Basic CRUD Tests ==========
    
    @Test
    void testGetAllRecipes_WhenRecipesExist_ThenReturnsAllRecipes() {
        // Arrange
        List<Recipe> recipes = Arrays.asList(testRecipe, testRecipe2);
        when(recipeRepository.findAll()).thenReturn(recipes);
        
        // Act
        List<Recipe> result = recipeService.getAllRecipes();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Pasta", result.get(0).getName());
        assertEquals("Tacos", result.get(1).getName());
        verify(recipeRepository).findAll();
    }
    
    @Test
    void testGetAllRecipes_WhenNoRecipesExist_ThenReturnsEmptyList() {
        // Arrange
        when(recipeRepository.findAll()).thenReturn(Collections.emptyList());
        
        // Act
        List<Recipe> result = recipeService.getAllRecipes();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(recipeRepository).findAll();
    }
    
    @Test
    void testGetRecipeById_WhenRecipeExists_ThenReturnsRecipe() {
        // Arrange
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        
        // Act
        Optional<Recipe> result = recipeService.getRecipeById(1L);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Pasta", result.get().getName());
        assertEquals("Italian", result.get().getCuisineType());
        verify(recipeRepository).findById(1L);
    }
    
    @Test
    void testGetRecipeById_WhenRecipeDoesNotExist_ThenReturnsEmpty() {
        // Arrange
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        Optional<Recipe> result = recipeService.getRecipeById(999L);
        
        // Assert
        assertFalse(result.isPresent());
        verify(recipeRepository).findById(999L);
    }
    
    @Test
    void testSaveRecipe_WhenValidRecipe_ThenSavesAndReturnsRecipe() {
        // Arrange
        Recipe newRecipe = new Recipe("Burger", "American burger", 10, 10, 1, "Easy", "American");
        when(recipeRepository.save(newRecipe)).thenReturn(newRecipe);
        
        // Act
        Recipe result = recipeService.saveRecipe(newRecipe);
        
        // Assert
        assertNotNull(result);
        assertEquals("Burger", result.getName());
        verify(recipeRepository).save(newRecipe);
    }
    
    @Test
    void testDeleteRecipe_WhenValidId_ThenDeletesCalled() {
        // Arrange
        doNothing().when(recipeRepository).deleteById(1L);
        
        // Act
        recipeService.deleteRecipe(1L);
        
        // Assert
        verify(recipeRepository).deleteById(1L);
    }
    
    // ========== Filter Tests ==========
    
    @Test
    void testGetRecipesByDifficulty_WhenManyRecipesExist_ThenReturnsMatchingDifficulty() {
        // Arrange
        List<Recipe> easyRecipes = Arrays.asList(testRecipe, testRecipe2);
        when(recipeRepository.findByDifficultyLevel("Easy")).thenReturn(easyRecipes);
        
        // Act
        List<Recipe> result = recipeService.getRecipesByDifficulty("Easy");
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> "Easy".equals(r.getDifficultyLevel())));
        verify(recipeRepository).findByDifficultyLevel("Easy");
    }
    
    @Test
    void testGetRecipesByDifficulty_WhenNoMatchingRecipes_ThenReturnsEmptyList() {
        // Arrange
        when(recipeRepository.findByDifficultyLevel("Hard")).thenReturn(Collections.emptyList());
        
        // Act
        List<Recipe> result = recipeService.getRecipesByDifficulty("Hard");
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(recipeRepository).findByDifficultyLevel("Hard");
    }
    
    @Test
    void testGetRecipesByCuisine_WhenMatchingRecipesExist_ThenReturnsMatchingCuisine() {
        // Arrange
        List<Recipe> italianRecipes = Collections.singletonList(testRecipe);
        when(recipeRepository.findByCuisineType("Italian")).thenReturn(italianRecipes);
        
        // Act
        List<Recipe> result = recipeService.getRecipesByCuisine("Italian");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Italian", result.get(0).getCuisineType());
        verify(recipeRepository).findByCuisineType("Italian");
    }
    
    @Test
    void testSearchRecipes_WhenSearchTermMatches_ThenReturnsMatchingRecipes() {
        // Arrange
        List<Recipe> searchResults = Collections.singletonList(testRecipe);
        when(recipeRepository.findByNameContainingIgnoreCase("pasta")).thenReturn(searchResults);
        
        // Act
        List<Recipe> result = recipeService.searchRecipes("pasta");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pasta", result.get(0).getName());
        verify(recipeRepository).findByNameContainingIgnoreCase("pasta");
    }
    
    @Test
    void testSearchRecipes_WhenNoMatches_ThenReturnsEmptyList() {
        // Arrange
        when(recipeRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(Collections.emptyList());
        
        // Act
        List<Recipe> result = recipeService.searchRecipes("xyz");
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(recipeRepository).findByNameContainingIgnoreCase("xyz");
    }
    
    // ========== Daily Recipe Tests ==========
    
    @Test
    void testGetDailyRecipe_WhenRecipesExist_ThenReturnsDeterministicRecipe() {
        // Arrange
        List<Recipe> recipes = Arrays.asList(testRecipe, testRecipe2);
        when(recipeRepository.findAll()).thenReturn(recipes);
        
        // Act
        Optional<Recipe> result1 = recipeService.getDailyRecipe();
        Optional<Recipe> result2 = recipeService.getDailyRecipe();
        
        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        // Same day should return same recipe
        assertEquals(result1.get().getId(), result2.get().getId());
        verify(recipeRepository, times(2)).findAll();
    }
    
    @Test
    void testGetDailyRecipe_WhenNoRecipesExist_ThenReturnsEmpty() {
        // Arrange
        when(recipeRepository.findAll()).thenReturn(Collections.emptyList());
        
        // Act
        Optional<Recipe> result = recipeService.getDailyRecipe();
        
        // Assert
        assertFalse(result.isPresent());
        verify(recipeRepository).findAll();
    }
    
    // ========== Pantry Recommendation Tests ==========
    
    @Test
    void testRecommendRecipesByPantryIngredients_WhenPantryHasMatches_ThenReturnsSortedByMatch() {
        // Arrange
        Long userId = 1L;
        List<UserPantry> pantryItems = Collections.singletonList(testPantryItem);
        List<Recipe> allRecipes = Arrays.asList(testRecipe, testRecipe2);
        
        when(userPantryRepository.findByUserId(userId)).thenReturn(pantryItems);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.recommendRecipesByPantryIngredients(userId);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // testRecipe has Pasta which matches the pantry
        assertTrue(result.stream().anyMatch(r -> "Pasta".equals(r.getName())));
        verify(userPantryRepository).findByUserId(userId);
        verify(recipeRepository).findAll();
    }
    
    @Test
    void testRecommendRecipesByPantryIngredients_WhenPantryIsEmpty_ThenReturnsAllRecipes() {
        // Arrange
        Long userId = 1L;
        List<Recipe> allRecipes = Arrays.asList(testRecipe, testRecipe2);
        
        when(userPantryRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.recommendRecipesByPantryIngredients(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(allRecipes.size(), result.size());
        verify(userPantryRepository).findByUserId(userId);
        verify(recipeRepository).findAll();
    }
    
    @Test
    void testFindCompleteRecipesByPantry_WhenAllIngredientsAvailable_ThenReturnsCompleteRecipes() {
        // Arrange
        Long userId = 1L;
        Ingredient pasta = new Ingredient();
        pasta.setId(1L);
        pasta.setName("Pasta");
        
        Ingredient tomato = new Ingredient();
        tomato.setId(2L);
        tomato.setName("Tomato");
        
        Ingredient garlic = new Ingredient();
        garlic.setId(3L);
        garlic.setName("Garlic");
        
        UserPantry pantry1 = new UserPantry(userId, pasta, 500.0, "grams");
        UserPantry pantry2 = new UserPantry(userId, tomato, 300.0, "grams");
        UserPantry pantry3 = new UserPantry(userId, garlic, 10.0, "cloves");
        
        List<UserPantry> pantryItems = Arrays.asList(pantry1, pantry2, pantry3);
        List<Recipe> allRecipes = Collections.singletonList(testRecipe);
        
        when(userPantryRepository.findByUserId(userId)).thenReturn(pantryItems);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.findCompleteRecipesByPantry(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pasta", result.get(0).getName());
        verify(userPantryRepository).findByUserId(userId);
        verify(recipeRepository).findAll();
    }
    
    @Test
    void testFindCompleteRecipesByPantry_WhenMissingIngredients_ThenReturnsEmptyList() {
        // Arrange
        Long userId = 1L;
        UserPantry pantry = new UserPantry(userId, testIngredient, 500.0, "grams");
        List<UserPantry> pantryItems = Collections.singletonList(pantry);
        List<Recipe> allRecipes = Collections.singletonList(testRecipe);
        
        when(userPantryRepository.findByUserId(userId)).thenReturn(pantryItems);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.findCompleteRecipesByPantry(userId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userPantryRepository).findByUserId(userId);
        verify(recipeRepository).findAll();
    }
    
    @Test
    void testFindRecipesMissingFewIngredients_WhenRecipesCloseToComplete_ThenReturnsNearlyComplete() {
        // Arrange
        Long userId = 1L;
        Ingredient pasta = new Ingredient();
        pasta.setId(1L);
        pasta.setName("Pasta");
        
        Ingredient tomato = new Ingredient();
        tomato.setId(2L);
        tomato.setName("Tomato");
        
        UserPantry pantry1 = new UserPantry(userId, pasta, 500.0, "grams");
        UserPantry pantry2 = new UserPantry(userId, tomato, 300.0, "grams");
        
        List<UserPantry> pantryItems = Arrays.asList(pantry1, pantry2);
        List<Recipe> allRecipes = Collections.singletonList(testRecipe); // Missing garlic
        
        when(userPantryRepository.findByUserId(userId)).thenReturn(pantryItems);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.findRecipesMissingFewIngredients(userId, 2);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pasta", result.get(0).getName());
        verify(userPantryRepository).findByUserId(userId);
        verify(recipeRepository).findAll();
    }
    
    @Test
    void testFindRecipesMissingFewIngredients_WhenTooManyMissing_ThenReturnsEmptyList() {
        // Arrange
        Long userId = 1L;
        Ingredient pasta = new Ingredient();
        pasta.setId(1L);
        pasta.setName("Pasta");
        
        UserPantry pantry = new UserPantry(userId, pasta, 500.0, "grams");
        List<UserPantry> pantryItems = Collections.singletonList(pantry);
        List<Recipe> allRecipes = Collections.singletonList(testRecipe); // Missing 2 ingredients
        
        when(userPantryRepository.findByUserId(userId)).thenReturn(pantryItems);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.findRecipesMissingFewIngredients(userId, 1);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userPantryRepository).findByUserId(userId);
        verify(recipeRepository).findAll();
    }
    
    // ========== Advanced Filter Tests ==========
    
    @Test
    void testGetRecipesByAdvancedFilter_WithDifficulty_ThenFiltersCorrectly() {
        // Arrange
        List<Recipe> allRecipes = Arrays.asList(testRecipe, testRecipe2);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        lenient().when(userPantryRepository.findByUserId(null)).thenReturn(Collections.emptyList());
        
        // Act
        List<Recipe> result = recipeService.getRecipesByAdvancedFilter(null, "Easy", null, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> "Easy".equals(r.getDifficultyLevel())));
    }
    
    @Test
    void testGetRecipesByAdvancedFilter_WithCuisineType_ThenFiltersCorrectly() {
        // Arrange
        List<Recipe> allRecipes = Arrays.asList(testRecipe, testRecipe2);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.getRecipesByAdvancedFilter(null, null, "Italian", null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Italian", result.get(0).getCuisineType());
    }
    
    @Test
    void testGetRecipesByAdvancedFilter_WithMaxCookingTime_ThenFiltersCorrectly() {
        // Arrange
        List<Recipe> allRecipes = Arrays.asList(testRecipe, testRecipe2); // Both 10+15=25 and 5+10=15 minutes
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.getRecipesByAdvancedFilter(null, null, null, 20, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tacos", result.get(0).getName()); // 5+10=15 <= 20
    }
    
    @Test
    void testGetRecipesByAdvancedFilter_WithMultipleFilters_ThenAppliesAndLogic() {
        // Arrange
        List<Recipe> allRecipes = Arrays.asList(testRecipe, testRecipe2);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act - Filter by Easy difficulty AND Italian cuisine
        List<Recipe> result = recipeService.getRecipesByAdvancedFilter(null, "Easy", "Italian", null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pasta", result.get(0).getName());
    }
    
    @Test
    void testGetRecipesByAdvancedFilter_WhenNoFiltersMatch_ThenReturnsEmptyList() {
        // Arrange
        List<Recipe> allRecipes = Arrays.asList(testRecipe, testRecipe2);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act - Filter by non-existent cuisine
        List<Recipe> result = recipeService.getRecipesByAdvancedFilter(null, null, "French", null, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    // ========== Convenience Method Tests ==========
    
    @Test
    void testGetRecipesByDifficultyAndCuisine_ThenCallsAdvancedFilterCorrectly() {
        // Arrange
        List<Recipe> allRecipes = Collections.singletonList(testRecipe);
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.getRecipesByDifficultyAndCuisine("Easy", "Italian");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(recipeRepository).findAll();
    }
    
    @Test
    void testGetQuickRecipes_WhenRecipesWithinTimeLimit_ThenReturnsThem() {
        // Arrange
        List<Recipe> allRecipes = Arrays.asList(testRecipe, testRecipe2); // 25 and 15 minutes
        when(recipeRepository.findAll()).thenReturn(allRecipes);
        
        // Act
        List<Recipe> result = recipeService.getQuickRecipes(20);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tacos", result.get(0).getName());
        verify(recipeRepository).findAll();
    }
    
    // ============= Rating Tests =============
    
    @Test
    void testAddRating_WhenFirstRating_ThenReturnsCorrectAverage() {
        // Arrange
        testRecipe.setAverageRating(0.0);
        testRecipe.setRatingCount(0);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        
        // Act
        Recipe result = recipeService.addRating(1L, 5);
        
        // Assert
        assertEquals(5.0, result.getAverageRating());
        assertEquals(1, result.getRatingCount());
        verify(recipeRepository, times(1)).save(testRecipe);
    }
    
    @Test
    void testAddRating_WhenMultipleRatings_ThenCalculatesCorrectAverage() {
        // Arrange
        testRecipe.setAverageRating(4.5);
        testRecipe.setRatingCount(4);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        
        // Act
        Recipe result = recipeService.addRating(1L, 3);
        
        // Assert
        // Expected: ((4.5 * 4) + 3) / 5 = 4.2
        assertEquals(4.2, result.getAverageRating(), 0.01);
        assertEquals(5, result.getRatingCount());
        verify(recipeRepository, times(1)).save(testRecipe);
    }
    
    @Test
    void testAddRating_WhenRatingLessThan1_ThenThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            recipeService.addRating(1L, 0);
        });
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenRatingGreaterThan5_ThenThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            recipeService.addRating(1L, 6);
        });
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenRecipeNotFound_ThenThrowsNoSuchElementException() {
        // Arrange
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            recipeService.addRating(999L, 5);
        });
        verify(recipeRepository, never()).save(any());
    }
    
    @Test
    void testAddRating_WhenValidRating_ThenVerifiesTransactional() {
        // Arrange
        testRecipe.setAverageRating(3.0);
        testRecipe.setRatingCount(2);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        
        // Act
        Recipe result = recipeService.addRating(1L, 4);
        
        // Assert
        // Expected: ((3.0 * 2) + 4) / 3 = 10/3 = 3.333...
        assertEquals(3.33, result.getAverageRating(), 0.01);
        assertEquals(3, result.getRatingCount());
    }
}
