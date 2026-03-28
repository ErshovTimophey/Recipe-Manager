package com.recipemanager.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Краткая информация о рецепте в списке")
public record RecipeSummaryResponse(
        long id,
        String title,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
