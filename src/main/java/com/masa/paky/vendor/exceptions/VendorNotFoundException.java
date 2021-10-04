package com.masa.paky.vendor.exceptions;

public class VendorNotFoundException extends RuntimeException{
    public VendorNotFoundException(String vendorId) {
        super("vendor not found: " + vendorId);
    }
}
