package com.recipemanager.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Ингредиент в составе рецепта")
public record RecipeIngredientRequest(
        @NotBlank @Schema(example = "Мука") String name,
        @NotNull @Positive @Schema(example = "200") BigDecimal quantity,
        @NotBlank @Schema(example = "г") String unit) {
}
