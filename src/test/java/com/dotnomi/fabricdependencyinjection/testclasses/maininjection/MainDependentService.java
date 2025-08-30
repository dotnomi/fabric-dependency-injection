package com.dotnomi.fabricdependencyinjection.testclasses.maininjection;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class MainDependentService {
  private final MainInjectionTestModMain mainInstance;

  @ModInject
  public MainDependentService(MainInjectionTestModMain mainInstance) {
    this.mainInstance = mainInstance;
  }

  public MainInjectionTestModMain getMainInstance() { return mainInstance; }
}
