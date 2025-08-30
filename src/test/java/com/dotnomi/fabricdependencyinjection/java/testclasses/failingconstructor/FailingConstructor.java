package com.dotnomi.fabricdependencyinjection.java.testclasses.failingconstructor;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class FailingConstructor {
  public FailingConstructor() {
    throw new IllegalStateException("Constructor failed");
  }
}
