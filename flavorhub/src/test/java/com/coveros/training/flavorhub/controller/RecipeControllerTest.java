package com.coveros.training.flavorhub.controller;

import com.coveros.training.flavorhub.dto.RatingRequest;
import com.coveros.training.flavorhub.model.Recipe;
import com.coveros.training.flavorhub.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RecipeController rating endpoint
 */
@WebMvcTest(RecipeController.class)
class RecipeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
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
        testRecipe.setAverageRating(4.5);
        testRecipe.setRatingCount(4);
    }
    
    @Test
    void testRateRecipe_WhenValidRating_ThenReturnsUpdatedRecipe() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(5);
        Recipe updatedRecipe = new Recipe(
            "Test Recipe",
            "Test Description",
            10,
            15,
            4,
            "Easy",
            "Italian"
        );
        updatedRecipe.setId(1L);
        updatedRecipe.setAverageRating(4.6);
        updatedRecipe.setRatingCount(5);
        
        when(recipeService.addRating(eq(1L), eq(5))).thenReturn(updatedRecipe);
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/1/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.averageRating").value(4.6))
                .andExpect(jsonPath("$.ratingCount").value(5));
    }
    
    @Test
    void testRateRecipe_WhenRatingLessThan1_ThenReturnsBadRequest() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(0);
        
        when(recipeService.addRating(eq(1L), eq(0)))
                .thenThrow(new IllegalArgumentException("Rating must be between 1 and 5"));
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/1/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testRateRecipe_WhenRatingGreaterThan5_ThenReturnsBadRequest() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(6);
        
        when(recipeService.addRating(eq(1L), eq(6)))
                .thenThrow(new IllegalArgumentException("Rating must be between 1 and 5"));
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/1/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testRateRecipe_WhenRecipeNotFound_ThenReturnsNotFound() throws Exception {
        // Arrange
        RatingRequest request = new RatingRequest(5);
        
        when(recipeService.addRating(eq(999L), eq(5)))
                .thenThrow(new NoSuchElementException("Recipe not found"));
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/999/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testRateRecipe_WhenNullRating_ThenReturnsBadRequest() throws Exception {
        // Arrange
        String invalidRequest = "{}";
        
        // Act & Assert
        mockMvc.perform(put("/api/recipes/1/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}
