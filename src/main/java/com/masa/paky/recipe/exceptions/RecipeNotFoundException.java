package com.masa.paky.recipe.exceptions;



public class RecipeNotFoundException extends RuntimeException{
    public RecipeNotFoundException(String recipeId) {
        super(
                getErrorMessage(recipeId)
        );
    }

    private static String getErrorMessage(String recipeId) {
        return String.format("Recipe not configured for machinery %s",recipeId );
    }
}
