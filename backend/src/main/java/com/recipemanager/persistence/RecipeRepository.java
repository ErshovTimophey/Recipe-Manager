package com.recipemanager.persistence;

import static com.recipemanager.generated.tables.Ingredients.INGREDIENTS;
import static com.recipemanager.generated.tables.RecipeIngredients.RECIPE_INGREDIENTS;
import static com.recipemanager.generated.tables.Recipes.RECIPES;

import com.recipemanager.generated.tables.records.RecipesRecord;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
public class RecipeRepository {

    private final DSLContext dsl;

    public RecipeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public record RecipeIngredientRow(long ingredientId, String name, BigDecimal quantity, String unit) {
    }

    public record RecipeDetail(
            long id,
            long userId,
            String title,
            String description,
            String instructions,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            List<RecipeIngredientRow> ingredients) {
    }

    public record RecipeSummaryRow(
            long id, String title, String description, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    }

    public List<RecipeSummaryRow> listSummariesByUserId(long userId) {
        return dsl.select(RECIPES.ID, RECIPES.TITLE, RECIPES.DESCRIPTION, RECIPES.CREATED_AT, RECIPES.UPDATED_AT)
                .from(RECIPES)
                .where(RECIPES.USER_ID.eq(userId))
                .orderBy(RECIPES.UPDATED_AT.desc())
                .fetch(r -> new RecipeSummaryRow(
                        r.get(RECIPES.ID),
                        r.get(RECIPES.TITLE),
                        r.get(RECIPES.DESCRIPTION),
                        r.get(RECIPES.CREATED_AT),
                        r.get(RECIPES.UPDATED_AT)));
    }

    public long insertRecipe(long userId, String title, String description, String instructions) {
        dsl.insertInto(RECIPES)
                .set(RECIPES.USER_ID, userId)
                .set(RECIPES.TITLE, title)
                .set(RECIPES.DESCRIPTION, description)
                .set(RECIPES.INSTRUCTIONS, instructions)
                .execute();
        return dsl.select(RECIPES.ID)
                .from(RECIPES)
                .where(RECIPES.USER_ID.eq(userId).and(RECIPES.TITLE.eq(title)))
                .orderBy(RECIPES.ID.desc())
                .limit(1)
                .fetchSingle(RECIPES.ID);
    }

    public Optional<RecipeDetail> findByIdAndUserId(long recipeId, long userId) {
        Optional<RecipesRecord> recipe = dsl.selectFrom(RECIPES)
                .where(RECIPES.ID.eq(recipeId).and(RECIPES.USER_ID.eq(userId)))
                .fetchOptional();
        if (recipe.isEmpty()) {
            return Optional.empty();
        }
        RecipesRecord r = recipe.get();
        List<RecipeIngredientRow> ingredients = dsl
                .select(INGREDIENTS.ID, INGREDIENTS.NAME, RECIPE_INGREDIENTS.QUANTITY, RECIPE_INGREDIENTS.UNIT)
                .from(RECIPE_INGREDIENTS)
                .join(INGREDIENTS).on(RECIPE_INGREDIENTS.INGREDIENT_ID.eq(INGREDIENTS.ID))
                .where(RECIPE_INGREDIENTS.RECIPE_ID.eq(recipeId))
                .orderBy(INGREDIENTS.NAME)
                .fetch(RecipeRepository::mapIngredientRow);
        return Optional.of(new RecipeDetail(
                r.getId(),
                r.getUserId(),
                r.getTitle(),
                r.getDescription(),
                r.getInstructions(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                ingredients));
    }

    private static RecipeIngredientRow mapIngredientRow(Record row) {
        return new RecipeIngredientRow(
                row.get(INGREDIENTS.ID),
                row.get(INGREDIENTS.NAME),
                row.get(RECIPE_INGREDIENTS.QUANTITY),
                row.get(RECIPE_INGREDIENTS.UNIT));
    }

    public void updateRecipe(long recipeId, long userId, String title, String description, String instructions) {
        dsl.update(RECIPES)
                .set(RECIPES.TITLE, title)
                .set(RECIPES.DESCRIPTION, description)
                .set(RECIPES.INSTRUCTIONS, instructions)
                .set(RECIPES.UPDATED_AT, OffsetDateTime.now(ZoneOffset.UTC))
                .where(RECIPES.ID.eq(recipeId).and(RECIPES.USER_ID.eq(userId)))
                .execute();
    }

    public int deleteRecipe(long recipeId, long userId) {
        return dsl.deleteFrom(RECIPES)
                .where(RECIPES.ID.eq(recipeId).and(RECIPES.USER_ID.eq(userId)))
                .execute();
    }

    public void deleteIngredientsForRecipe(long recipeId) {
        dsl.deleteFrom(RECIPE_INGREDIENTS).where(RECIPE_INGREDIENTS.RECIPE_ID.eq(recipeId)).execute();
    }

    public long findOrCreateIngredient(String name) {
        String normalized = name.trim();
        return dsl.select(INGREDIENTS.ID)
                .from(INGREDIENTS)
                .where(INGREDIENTS.NAME.eq(normalized))
                .fetchOptional(INGREDIENTS.ID)
                .orElseGet(() -> {
                    dsl.insertInto(INGREDIENTS).set(INGREDIENTS.NAME, normalized).execute();
                    return dsl.select(INGREDIENTS.ID)
                            .from(INGREDIENTS)
                            .where(INGREDIENTS.NAME.eq(normalized))
                            .fetchSingle(INGREDIENTS.ID);
                });
    }

    public void insertRecipeIngredient(long recipeId, long ingredientId, BigDecimal quantity, String unit) {
        dsl.insertInto(RECIPE_INGREDIENTS)
                .set(RECIPE_INGREDIENTS.RECIPE_ID, recipeId)
                .set(RECIPE_INGREDIENTS.INGREDIENT_ID, ingredientId)
                .set(RECIPE_INGREDIENTS.QUANTITY, quantity)
                .set(RECIPE_INGREDIENTS.UNIT, unit.trim())
                .execute();
    }
}
