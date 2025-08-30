package com.dotnomi.fabricdependencyinjection.exception;

public final class TooManyInstancesFoundException extends ModInjectorException {
  public TooManyInstancesFoundException(String message) {
    super(message);
  }
}
