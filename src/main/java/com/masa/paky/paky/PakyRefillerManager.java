package com.masa.paky.paky;

import com.masa.paky.machinery.entity.MachineryRepository;
import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.exceptions.PakyNotFoundException;
import com.masa.paky.recipe.entity.Recipe;
import com.masa.paky.recipe.exceptions.RecipeNotFoundException;
import lombok.RequiredArgsConstructor;

import static com.masa.paky.paky.entity.ErrorStatus.FILLED_WITH_UNKNOWN_PRODUCT;
import static com.masa.paky.paky.entity.TraciabilityStatus.ERROR;

@RequiredArgsConstructor
public class PakyRefillerManager {


    public final PakyRepository pakyRepository;
    public final MachineryRepository machineryRepository;

    public void refill(String machineryId, String pakyId){
        try {
            withLifeCicleHandlerFactory()
                    .getFor(get(pakyId))
                    .fill(getActiveRecipeOn(machineryId));
        } catch (RecipeNotFoundException unknownProductError){
            reportTraciabilityError(pakyId);
            throw unknownProductError;
        }
    }

    private void reportTraciabilityError(String pakyId) {
        final Paky paky = get(pakyId);
        paky.setTraciabilityStatus(ERROR);
        paky.setErrorCode(FILLED_WITH_UNKNOWN_PRODUCT);
        pakyRepository.update(paky);
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
