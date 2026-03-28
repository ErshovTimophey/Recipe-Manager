package com.recipemanager.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Вход")
public record LoginRequest(
        @Email @NotBlank @Schema(example = "cook@example.com") String email,
        @NotBlank @Schema(example = "password123") String password) {
}
