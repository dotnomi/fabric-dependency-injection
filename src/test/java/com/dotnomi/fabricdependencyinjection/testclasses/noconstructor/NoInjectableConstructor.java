package com.dotnomi.fabricdependencyinjection.testclasses.noconstructor;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
@SuppressWarnings("unused")
public class NoInjectableConstructor {
  private NoInjectableConstructor() {}
}
