package com.masa.paky.recipe.entity;

import java.util.Optional;

public interface RecipeRepository {
    Optional<Recipe> findById(String id);
    void save(Recipe recipe);
}
