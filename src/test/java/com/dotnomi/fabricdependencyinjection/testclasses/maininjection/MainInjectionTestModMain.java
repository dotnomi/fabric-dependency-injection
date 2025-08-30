package com.dotnomi.fabricdependencyinjection.testclasses.maininjection;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModMain;
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct;

@ModMain
public class MainInjectionTestModMain {
  @ModInject
  private ServiceA serviceA;

  private boolean postConstructCalled = false;

  @PostConstruct
  private void initialize() {
    this.postConstructCalled = true;
  }

  public ServiceA getServiceA() { return serviceA; }
  public boolean isPostConstructCalled() { return postConstructCalled; }
}
