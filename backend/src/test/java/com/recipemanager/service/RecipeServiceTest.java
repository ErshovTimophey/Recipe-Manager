package com.recipemanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.recipemanager.api.dto.RecipeIngredientRequest;
import com.recipemanager.api.dto.RecipeRequest;
import com.recipemanager.api.dto.RecipeResponse;
import com.recipemanager.persistence.RecipeRepository;
import com.recipemanager.persistence.RecipeRepository.RecipeDetail;
import com.recipemanager.persistence.RecipeRepository.RecipeIngredientRow;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    RecipeRepository recipeRepository;

    @InjectMocks
    RecipeService recipeService;

    @Test
    void getThrowsWhenMissing() {
        when(recipeRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> recipeService.get(1L, 9L))
                .isInstanceOf(RecipeNotFoundException.class)
                .hasFieldOrPropertyWithValue("recipeId", 9L);
    }

    @Test
    void createInsertsRecipeAndIngredients() {
        RecipeRequest req = new RecipeRequest(
                "Soup",
                "Nice",
                "Boil",
                List.of(new RecipeIngredientRequest("Salt", BigDecimal.ONE, "ч.л.")));
        when(recipeRepository.insertRecipe(1L, "Soup", "Nice", "Boil")).thenReturn(100L);
        when(recipeRepository.findOrCreateIngredient("Salt")).thenReturn(5L);
        OffsetDateTime t = OffsetDateTime.now(ZoneOffset.UTC);
        RecipeDetail detail = new RecipeDetail(
                100L,
                1L,
                "Soup",
                "Nice",
                "Boil",
                t,
                t,
                List.of(new RecipeIngredientRow(5L, "Salt", BigDecimal.ONE, "ч.л.")));
        when(recipeRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(detail));

        RecipeResponse out = recipeService.create(1L, req);

        assertThat(out.id()).isEqualTo(100L);
        assertThat(out.ingredients()).hasSize(1);
        verify(recipeRepository).insertRecipe(1L, "Soup", "Nice", "Boil");
        verify(recipeRepository).insertRecipeIngredient(100L, 5L, BigDecimal.ONE, "ч.л.");
    }

    @Test
    void deleteThrowsWhenNoRow() {
        when(recipeRepository.deleteRecipe(3L, 1L)).thenReturn(0);
        assertThatThrownBy(() -> recipeService.delete(1L, 3L)).isInstanceOf(RecipeNotFoundException.class);
    }
}
