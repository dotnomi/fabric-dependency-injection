package com.dotnomi.fabricdependencyinjection.java.testclasses.success;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class ServiceA {
  private final ServiceB serviceB;

  @ModInject
  public ServiceA(ServiceB serviceB) {
    this.serviceB = serviceB;
  }

  public ServiceB getServiceB() {
    return serviceB;
  }
}
