package com.recipemanager.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Регистрация пользователя")
public record RegisterRequest(
        @Email @NotBlank @Schema(example = "cook@example.com") String email,
        @NotBlank @Size(min = 8, max = 128) @Schema(example = "password123") String password,
        @NotBlank @Size(max = 100) @Schema(example = "chef") String username) {
}
