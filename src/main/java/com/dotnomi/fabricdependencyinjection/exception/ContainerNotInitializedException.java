package com.dotnomi.fabricdependencyinjection.exception;

public class ContainerNotInitializedException extends ModInjectorException {
  public ContainerNotInitializedException(String message) {
    super(message);
  }
}
