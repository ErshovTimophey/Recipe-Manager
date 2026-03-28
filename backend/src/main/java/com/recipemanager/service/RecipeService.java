package com.recipemanager.service;

import com.recipemanager.api.dto.RecipeIngredientRequest;
import com.recipemanager.api.dto.RecipeIngredientResponse;
import com.recipemanager.api.dto.RecipeRequest;
import com.recipemanager.api.dto.RecipeResponse;
import com.recipemanager.api.dto.RecipeSummaryResponse;
import com.recipemanager.persistence.RecipeRepository;
import com.recipemanager.persistence.RecipeRepository.RecipeDetail;
import com.recipemanager.persistence.RecipeRepository.RecipeIngredientRow;
import com.recipemanager.persistence.RecipeRepository.RecipeSummaryRow;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {

    private static final Logger log = LoggerFactory.getLogger(RecipeService.class);

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public RecipeResponse create(long userId, RecipeRequest request) {
        long recipeId = recipeRepository.insertRecipe(
                userId, request.title(), request.description(), request.instructions());
        saveIngredients(recipeId, request.ingredients());
        log.info("Recipe created id={} userId={}", recipeId, userId);
        return recipeRepository
                .findByIdAndUserId(recipeId, userId)
                .map(RecipeService::toResponse)
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<RecipeSummaryResponse> list(long userId) {
        return recipeRepository.listSummariesByUserId(userId).stream()
                .map(RecipeService::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeResponse get(long userId, long recipeId) {
        return recipeRepository
                .findByIdAndUserId(recipeId, userId)
                .map(RecipeService::toResponse)
                .orElseThrow(() -> new RecipeNotFoundException(recipeId));
    }

    @Transactional
    public RecipeResponse update(long userId, long recipeId, RecipeRequest request) {
        if (recipeRepository.findByIdAndUserId(recipeId, userId).isEmpty()) {
            throw new RecipeNotFoundException(recipeId);
        }
        recipeRepository.updateRecipe(
                recipeId, userId, request.title(), request.description(), request.instructions());
        recipeRepository.deleteIngredientsForRecipe(recipeId);
        saveIngredients(recipeId, request.ingredients());
        log.info("Recipe updated id={} userId={}", recipeId, userId);
        return recipeRepository
                .findByIdAndUserId(recipeId, userId)
                .map(RecipeService::toResponse)
                .orElseThrow(() -> new RecipeNotFoundException(recipeId));
    }

    @Transactional
    public void delete(long userId, long recipeId) {
        int n = recipeRepository.deleteRecipe(recipeId, userId);
        if (n == 0) {
            throw new RecipeNotFoundException(recipeId);
        }
        log.info("Recipe deleted id={} userId={}", recipeId, userId);
    }

    private void saveIngredients(long recipeId, List<RecipeIngredientRequest> ingredients) {
        for (RecipeIngredientRequest line : ingredients) {
            long ingId = recipeRepository.findOrCreateIngredient(line.name());
            recipeRepository.insertRecipeIngredient(recipeId, ingId, line.quantity(), line.unit());
        }
    }

    private static RecipeResponse toResponse(RecipeDetail d) {
        List<RecipeIngredientResponse> ing = d.ingredients().stream()
                .map(RecipeService::toIngredientResponse)
                .toList();
        return new RecipeResponse(
                d.id(),
                d.title(),
                d.description(),
                d.instructions(),
                d.createdAt(),
                d.updatedAt(),
                ing);
    }

    private static RecipeIngredientResponse toIngredientResponse(RecipeIngredientRow r) {
        return new RecipeIngredientResponse(r.ingredientId(), r.name(), r.quantity(), r.unit());
    }

    private static RecipeSummaryResponse toSummary(RecipeSummaryRow r) {
        return new RecipeSummaryResponse(r.id(), r.title(), r.description(), r.createdAt(), r.updatedAt());
    }
}
