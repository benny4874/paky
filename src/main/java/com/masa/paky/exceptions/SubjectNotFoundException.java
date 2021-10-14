package com.masa.paky.exceptions;

public abstract class SubjectNotFoundException extends RuntimeException {
  public SubjectNotFoundException(String message) {
    super(message);
  }
}
