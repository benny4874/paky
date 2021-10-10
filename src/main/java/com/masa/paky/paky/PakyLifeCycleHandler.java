package com.masa.paky.paky;

import static com.masa.paky.paky.entity.PakyStatus.*;
import static lombok.AccessLevel.PACKAGE;

import com.masa.paky.paky.entity.Paky;
import com.masa.paky.paky.entity.PakyRepository;
import com.masa.paky.recipe.entity.Recipe;
import io.micronaut.core.annotation.Introspected;
import java.util.Date;
import lombok.AllArgsConstructor;

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
    paky.setQuantity(quantity);
    save();
  }
}
