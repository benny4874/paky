package com.masa.paky.recipe.exceptions;

public class NoExpirationDateException extends WrongRecipeException {

    public NoExpirationDateException(String message) {
        super(message);
    }
}
