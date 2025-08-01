package com.dotnomi.fabricdependencyinjection.testclasses.circular;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
@SuppressWarnings("unused")
public class CircularB {
  @ModInject
  public CircularB(CircularA circularA) {}
}
