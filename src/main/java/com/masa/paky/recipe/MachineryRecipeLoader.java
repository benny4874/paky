package com.masa.paky.recipe;

import com.masa.paky.machinery.entity.Machinery;
import com.masa.paky.machinery.entity.MachineryRepository;
import com.masa.paky.machinery.exception.MachineryNotFoundException;
import com.masa.paky.recipe.entity.Recipe;
import com.masa.paky.recipe.entity.RecipeRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MachineryRecipeLoader {
    private final Recipe recipe;
    private final Machinery machinery;
    private final RecipeRepository recipeRepository;
    private final MachineryRepository machineryRepository;

    public void load() {
        checkMachineryExists();
        recipeRepository.save(recipe);
        machinery.setRecipeId(recipe.getRecipeId());
        machineryRepository.update(machinery);
    }

    private void checkMachineryExists() {
        machineryRepository
                .findById(machinery.getMachineryId())
                .orElseThrow(() ->new MachineryNotFoundException(machinery.getMachineryId()));
    }
}
