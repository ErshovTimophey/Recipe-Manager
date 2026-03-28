package com.recipemanager.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Рецепт")
public record RecipeResponse(
        long id,
        String title,
        String description,
        String instructions,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<RecipeIngredientResponse> ingredients) {
}
