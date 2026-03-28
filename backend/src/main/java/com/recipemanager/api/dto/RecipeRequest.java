package com.recipemanager.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Создание или обновление рецепта")
public record RecipeRequest(
        @NotBlank @Schema(example = "Борщ") String title,
        @Schema(example = "Классический") String description,
        @Schema(example = "Варить 40 минут") String instructions,
        @NotNull @Valid @Schema(description = "Список ингредиентов (может быть пустым)") List<RecipeIngredientRequest> ingredients) {
}
