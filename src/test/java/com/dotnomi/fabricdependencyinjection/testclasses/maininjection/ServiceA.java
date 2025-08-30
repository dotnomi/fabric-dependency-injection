package com.dotnomi.fabricdependencyinjection.testclasses.maininjection;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class ServiceA {
  public String sayHello() {
    return "Hello from ServiceA";
  }
}
