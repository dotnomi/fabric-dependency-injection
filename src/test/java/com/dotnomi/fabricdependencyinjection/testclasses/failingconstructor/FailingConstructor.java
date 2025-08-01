package com.dotnomi.fabricdependencyinjection.testclasses.failingconstructor;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
@SuppressWarnings("unused")
public class FailingConstructor {
  public FailingConstructor() {
    throw new IllegalStateException("Constructor failed");
  }
}
