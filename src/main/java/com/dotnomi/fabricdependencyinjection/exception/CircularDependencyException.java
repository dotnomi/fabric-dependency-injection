package com.dotnomi.fabricdependencyinjection.exception;

import java.util.List;

public class CircularDependencyException extends ModInjectorException {
  public CircularDependencyException(List<Class<?>> dependencyPath) {
    super(CircularDependencyFormatter.format(dependencyPath));
  }
}
