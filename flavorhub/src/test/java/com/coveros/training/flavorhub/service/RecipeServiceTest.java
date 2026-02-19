package com.coveros.training.flavorhub.service;

import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecipeService
 */
@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {
    
    @Mock
    private RecipeRepository recipeRepository;
    
    @InjectMocks
    private RecipeService recipeService;
    
    private Recipe testRecipe;
    
    @BeforeEach
    void setUp() {
        testRecipe = new Recipe(
            "Test Recipe",
            "Test Description",
            10,
            15,
            4,
            "Easy",
            "Italian"
        );
        testRecipe.setId(1L);
    }
    
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
