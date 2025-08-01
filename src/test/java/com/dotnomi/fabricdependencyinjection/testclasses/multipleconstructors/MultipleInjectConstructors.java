package com.dotnomi.fabricdependencyinjection.testclasses.multipleconstructors;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
@SuppressWarnings("unused")
public class MultipleInjectConstructors {
  @ModInject
  public MultipleInjectConstructors(ServiceA serviceA) {}

  @ModInject
  public MultipleInjectConstructors(ServiceA serviceA, ServiceB serviceB) {}
}
