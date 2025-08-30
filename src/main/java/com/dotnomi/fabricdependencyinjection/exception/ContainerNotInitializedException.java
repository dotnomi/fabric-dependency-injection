package com.dotnomi.fabricdependencyinjection.exception;

public final class ContainerNotInitializedException extends ModInjectorException {
  public ContainerNotInitializedException(String message) {
    super(message);
  }
}
