package com.dotnomi.fabricdependencyinjection.exception;

public final class InstanceCreationException extends ModInjectorException {
  public InstanceCreationException(String message) {
    super(message);
  }

  public InstanceCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
