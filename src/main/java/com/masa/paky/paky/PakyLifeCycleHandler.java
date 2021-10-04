package com.masa.paky.paky;

import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.recipe.entity.Recipe;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;

import java.util.Date;

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
        paky.setQuantita(recipe.getQuantity());
        paky.setProductTypeId(recipe.getDescription());
        paky.setQuantitaPct(1);
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
        if (isNew)
            pakyRepository.save(paky);
        else
            pakyRepository.update(paky);
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
        paky.setQuantita(quantity);
        save();
    }
}
