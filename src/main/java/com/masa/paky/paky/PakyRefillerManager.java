package com.masa.paky.paky;

import com.masa.paky.machinery.entity.MachineryRepository;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.recipe.entity.Recipe;
import com.masa.paky.recipe.exceptions.RecipeNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PakyRefillerManager {


    public final PakyRepository pakyRepository;
    public final MachineryRepository machineryRepository;

    public void refill(String machineryId, String pakyId){
        withLifeCicleHandlerFactory()
                .getFor(get(pakyId))
               .fill(getActiveRecipeOn(machineryId));

    }

    private PakyLifeCycleHandlerFactory withLifeCicleHandlerFactory() {
        return new PakyLifeCycleHandlerFactory(pakyRepository);
    }

    private Recipe getActiveRecipeOn(String machineryId) {
        return machineryRepository.findActualRecipeFor(machineryId)
                .orElseThrow(() -> new RecipeNotFoundException(machineryId));
    }

    private Paky get(String pakyId) {
       return  pakyRepository.findById(pakyId)
                .orElseThrow(() -> new PakyNotFoundException(pakyId));
    }
}
