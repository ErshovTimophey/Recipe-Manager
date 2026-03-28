package com.recipemanager.service;

public class RecipeNotFoundException extends RuntimeException {

    private final long recipeId;

    public RecipeNotFoundException(long recipeId) {
        super("Recipe not found: " + recipeId);
        this.recipeId = recipeId;
    }

    public long getRecipeId() {
        return recipeId;
    }
}
