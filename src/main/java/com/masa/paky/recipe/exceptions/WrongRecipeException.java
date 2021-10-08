package com.masa.paky.recipe.exceptions;

public class WrongRecipeException extends IllegalArgumentException {
  public WrongRecipeException(String message) {
    super(message);
  }
}
