package com.masa.paky.vendor.exceptions;

import com.masa.paky.exceptions.SubjectNotFoundException;

public class VendorNotFoundException extends SubjectNotFoundException {
  public VendorNotFoundException(String vendorId) {
    super("vendor not found: " + vendorId);
  }
}
