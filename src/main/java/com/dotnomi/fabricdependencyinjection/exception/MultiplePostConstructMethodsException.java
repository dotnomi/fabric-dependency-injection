package com.dotnomi.fabricdependencyinjection.exception;

public final class MultiplePostConstructMethodsException extends ModInjectorException {
  public MultiplePostConstructMethodsException(String message) {
    super(message);
  }
}
