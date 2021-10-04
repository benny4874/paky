package com.masa.paky.paky.exceptions;

public class PakyNotFoundException extends RuntimeException{
    public PakyNotFoundException(String pakyId) {
        super("paky with id: " + pakyId + " not found");
    }
}
