package com.masa.paky.paky.exceptions;

import static java.lang.String.format;

public class DestinationMissMatchException extends RuntimeException {
  public DestinationMissMatchException(String expectedCustomer, String realCustomer) {
    super(
        format(
            "Paky is meant to be sent to %s, but real destination is %s",
            expectedCustomer, realCustomer));
  }
}
