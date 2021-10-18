package com.masa.paky.base.exceptions;

public class BaseNotFoundException extends RuntimeException{
    public BaseNotFoundException(String baseId) {
        super("Base non trovata: " + baseId);
    }
}
