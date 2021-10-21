package com.masa.paky.paky;

import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.paky.entity.TraciabilityStatus;
import com.masa.paky.paky.exceptions.PakyNotPluggedException;
import com.masa.paky.recipe.entity.Recipe;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;

import java.util.Date;

import static com.masa.paky.paky.entity.ErrorStatus.INVALID_QUANTITY_REPORTED;
import static com.masa.paky.paky.entity.ErrorStatus.PAKY_NOT_RECEIVED_BUT_OPERATING;
import static com.masa.paky.paky.entity.PakyStatus.*;
import static lombok.AccessLevel.PACKAGE;

@AllArgsConstructor(access = PACKAGE)
@Introspected
public class PakyLifeCycleHandler {
    private final Paky paky;
    private final PakyRepository pakyRepository;
    private final boolean isNew;

    public void bookFor(String resellerId) {
        paky.setVendorId(resellerId);
        paky.setStep(ASSIGNED);
        save();
    }

    public void deliverToReseller() {
        paky.setStep(DELIVERED);
        save();
    }

    public void fill(Recipe recipe) {
        paky.setQuantity(recipe.getQuantity());
        paky.setOriginalQuantity(recipe.getQuantity());
        paky.setProductTypeId(recipe.getDescription());
        paky.setQuantityPct(1);
        paky.setPackingDate(new Date());
        paky.setStep(FILLED);
        paky.setLabel(recipe.getLabel());
        paky.setBrand(recipe.getBrand());
        paky.setUnit(recipe.getUnit());
        paky.setExpiration(recipe.getExpiration());
        save();
    }

    public void book(String customerId) {
        paky.setCustomerId(customerId);
        paky.setStep(SOLD);
        save();
    }

    private void save() {
        paky.setLastAction(new Date());
        if (isNew) pakyRepository.save(paky);
        else pakyRepository.update(paky);
    }

    public Paky get() {
        return paky;
    }

    public void sendToCustomer() {
        paky.setStep(INTRANSIT);
        save();
    }

    public void plug() {
        paky.setStep(OPERATING);
        save();
    }

    public void report(float quantity) {
        try {
            updateQuantity(quantity);
            checkIsReceived();
        } finally {
            save();
        }
    }





    private void checkIsReceived() {
        if (!paky.getStep().equals(OPERATING)) {
            paky.setTraciabilityStatus(TraciabilityStatus.ERROR);
            paky.setErrorCode(PAKY_NOT_RECEIVED_BUT_OPERATING);
            throw new PakyNotPluggedException(paky.getIdPaky());
        }
    }

    private void updateQuantity(float quantity) {
        if (isValidQuantity(quantity)) {
            handleError(quantity);
        } else {
            paky.setQuantityPct(quantity / paky.getOriginalQuantity());
        }
        paky.setQuantity(quantity);
    }

    private boolean isValidQuantity(float quantity) {
        return (quantity < 0) || (quantity > paky.getQuantity());
    }

    private void handleError(float quantity) {
        paky.setTraciabilityStatus(TraciabilityStatus.ERROR);
        paky.setErrorCode(INVALID_QUANTITY_REPORTED);
        paky.setQuantityPct(((quantity < 0)?0:1));
    }


}
