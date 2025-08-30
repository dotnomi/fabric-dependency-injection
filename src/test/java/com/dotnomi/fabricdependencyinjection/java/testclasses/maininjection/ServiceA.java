package com.dotnomi.fabricdependencyinjection.java.testclasses.maininjection;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class ServiceA {
  public String sayHello() {
    return "Hello from ServiceA";
  }
}
