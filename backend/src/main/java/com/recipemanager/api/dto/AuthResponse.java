package com.recipemanager.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT и данные пользователя")
public record AuthResponse(
        @Schema(description = "Bearer token") String token,
        long userId,
        String email,
        String username) {
}
