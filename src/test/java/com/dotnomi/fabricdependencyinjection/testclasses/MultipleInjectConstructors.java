package com.dotnomi.fabricdependencyinjection.testclasses;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;

public class MultipleInjectConstructors {
  @ModInject
  public MultipleInjectConstructors() {

  }

  @ModInject
  public MultipleInjectConstructors(ServiceA serviceA) {

  }
}
