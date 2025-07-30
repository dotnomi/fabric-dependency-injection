package com.dotnomi.fabricdependencyinjection.exception;

/**
 * Base exception for all errors originating from the ModInjector.
 */
public class ModInjectorException extends RuntimeException {
  public ModInjectorException(String message) {
    super(message);
  }

  public ModInjectorException(String message, Throwable cause) {
    super(message, cause);
  }
}
