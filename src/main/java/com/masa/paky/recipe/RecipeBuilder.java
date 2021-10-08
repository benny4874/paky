package com.masa.paky.recipe;

import static com.masa.utils.json.JSONUtils.isJSONValid;

import com.masa.paky.recipe.entity.Recipe;
import com.masa.paky.recipe.exceptions.InvalidLabelException;
import com.masa.paky.recipe.exceptions.InvalidQuantityException;
import com.masa.paky.recipe.exceptions.NoExpirationDateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class RecipeBuilder {
  private final String vendorId;
  public float quantity;
  public String description;
  public String unit;
  public String brand;
  public Date expiration;
  public String label;

  private RecipeBuilder(String vendorId) {
    this.vendorId = vendorId;
  }

  public static RecipeBuilder getFor(String vendorId) {
    return new RecipeBuilder(vendorId);
  }

  public RecipeBuilder with(Consumer<RecipeBuilder> builder) {
    builder.accept(this);
    return this;
  }

  public Recipe build() {
    return new Recipe(generateId(), quantity, description, unit, brand, expiration, label);
  }

  private String generateId() {
    checkField();
    return vendorId + (new SimpleDateFormat("yyyyMMddhhmmss"));
  }

  private void checkField() {
    checkQuantity();
    checkExpiration();
    checkLabel();
  }

  private void checkLabel() {
    if (label == null) throw new InvalidLabelException("Label is  null");
    if (!isJSONValid(label)) throw new InvalidLabelException("Label is in invalid json format");
  }

  private void checkExpiration() {
    if (expiration == null) throw new NoExpirationDateException("Expiration date is null");
    if ((new Date()).after(expiration))
      throw new NoExpirationDateException("Expiration date is invalid");
  }

  private void checkQuantity() {
    if (quantity == 0)
      throw new InvalidQuantityException("Quantity is invalid (must be greater than 0)");
    if (quantity < 0)
      throw new InvalidQuantityException("Quantity is invalid (must not be negative)");
  }
}
