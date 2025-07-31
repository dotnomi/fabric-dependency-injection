package com.dotnomi.fabricdependencyinjection.testclasses;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModMain;

@ModMain(packageName = "com.dotnomi.fabricdependencyinjection.testclasses")
public class TestModMain {
  @ModInject
  public ServiceA serviceA;
}
