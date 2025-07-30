package com.dotnomi.fabricdependencyinjection.exception;

public class MultipleInjectableConstructorsException extends ModInjectorException {
  public MultipleInjectableConstructorsException(String message) {
    super(message);
  }
}
