package com.dotnomi.fabricdependencyinjection.java.testclasses.circular;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class CircularA {
  @ModInject
  public CircularA(CircularB circularB) {}
}
