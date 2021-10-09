package com.masa.paky.customer.exceptions;

import com.masa.paky.exceptions.SubjectNotFoundException;

public class CustomerNotFoundException extends SubjectNotFoundException {
  public CustomerNotFoundException(String vendorId) {
    super("Customer not found: " + vendorId);
  }
}
