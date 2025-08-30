package com.dotnomi.fabricdependencyinjection.exception;

public final class ContainerAlreadyInitializedException extends ModInjectorException {
  public ContainerAlreadyInitializedException(String message) {
    super(message);
  }
}
