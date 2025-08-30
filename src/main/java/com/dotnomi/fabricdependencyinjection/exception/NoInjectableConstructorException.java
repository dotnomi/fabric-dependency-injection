package com.dotnomi.fabricdependencyinjection.exception;

public final class NoInjectableConstructorException extends ModInjectorException {
  public NoInjectableConstructorException(String message, Throwable cause) {
    super(message, cause);
  }
}
