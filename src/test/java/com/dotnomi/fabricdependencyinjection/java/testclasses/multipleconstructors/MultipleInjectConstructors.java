package com.dotnomi.fabricdependencyinjection.java.testclasses.multipleconstructors;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class MultipleInjectConstructors {
  @ModInject
  public MultipleInjectConstructors(ServiceA serviceA) {}

  @ModInject
  public MultipleInjectConstructors(ServiceA serviceA, ServiceB serviceB) {}
}
