package com.masa.paky.paky.exceptions;

public class PakyNotPluggedException extends IllegalStateException{
    public PakyNotPluggedException(String pakyId) {
        super("paky with id: " + pakyId + " is reporting, but it not result received by any customer");
    }
}
