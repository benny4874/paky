package com.masa.paky.machinery.exception;

public class MachineryNotFoundException extends RuntimeException{
    public MachineryNotFoundException(String machineryId) {
        super("Not existing machine with Id: " + machineryId);
    }
}
