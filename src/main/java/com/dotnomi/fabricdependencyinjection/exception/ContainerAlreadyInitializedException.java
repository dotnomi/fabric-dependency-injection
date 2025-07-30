package com.dotnomi.fabricdependencyinjection.exception;

public class ContainerAlreadyInitializedException extends ModInjectorException {
  public ContainerAlreadyInitializedException(String message) {
    super(message);
  }
}
