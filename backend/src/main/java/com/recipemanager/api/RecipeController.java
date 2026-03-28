package com.recipemanager.api;

import com.recipemanager.api.dto.RecipeRequest;
import com.recipemanager.api.dto.RecipeResponse;
import com.recipemanager.api.dto.RecipeSummaryResponse;
import java.util.List;
import com.recipemanager.security.AppUserDetails;
import com.recipemanager.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recipes", description = "CRUD рецептов (только свои рецепты)")
@SecurityRequirement(name = "bearerAuth")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    @Operation(summary = "ListRecipes", description = "Список рецептов текущего пользователя")
    public List<RecipeSummaryResponse> list(@AuthenticationPrincipal AppUserDetails user) {
        return recipeService.list(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "CreateRecipe", description = "Создание рецепта")
    public RecipeResponse create(
            @AuthenticationPrincipal AppUserDetails user, @Valid @RequestBody RecipeRequest request) {
        return recipeService.create(user.getId(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "GetRecipe", description = "Получение рецепта по id")
    public RecipeResponse get(@AuthenticationPrincipal AppUserDetails user, @PathVariable long id) {
        return recipeService.get(user.getId(), id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "UpdateRecipe", description = "Обновление рецепта")
    public RecipeResponse update(
            @AuthenticationPrincipal AppUserDetails user,
            @PathVariable long id,
            @Valid @RequestBody RecipeRequest request) {
        return recipeService.update(user.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "DeleteRecipe", description = "Удаление рецепта")
    public void delete(@AuthenticationPrincipal AppUserDetails user, @PathVariable long id) {
        recipeService.delete(user.getId(), id);
    }
}
