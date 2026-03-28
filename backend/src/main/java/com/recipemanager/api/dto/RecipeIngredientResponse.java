package com.recipemanager.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Ингредиент в ответе")
public record RecipeIngredientResponse(
        long ingredientId,
        String name,
        BigDecimal quantity,
        String unit) {
}
