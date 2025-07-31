package com.dotnomi.fabricdependencyinjection.testclasses;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class CircularB {
  @ModInject
  public CircularB(CircularA circularA) {

  }
}
