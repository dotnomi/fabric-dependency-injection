package com.dotnomi.fabricdependencyinjection.testclasses.noconstructor;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class NoInjectableConstructor {
  private NoInjectableConstructor() {}
}
