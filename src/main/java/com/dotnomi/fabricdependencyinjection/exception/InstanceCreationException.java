package com.dotnomi.fabricdependencyinjection.exception;

public class InstanceCreationException extends ModInjectorException {
  public InstanceCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
