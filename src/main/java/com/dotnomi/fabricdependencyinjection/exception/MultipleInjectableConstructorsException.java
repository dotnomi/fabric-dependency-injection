package com.dotnomi.fabricdependencyinjection.exception;

public final class MultipleInjectableConstructorsException extends ModInjectorException {
  public MultipleInjectableConstructorsException(String message) {
    super(message);
  }
}
