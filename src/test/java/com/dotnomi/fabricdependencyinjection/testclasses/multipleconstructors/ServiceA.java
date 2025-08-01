package com.dotnomi.fabricdependencyinjection.testclasses.multipleconstructors;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
@SuppressWarnings("unused")
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
