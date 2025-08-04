package com.dotnomi.fabricdependencyinjection.testclasses.success;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModMain;

@ModMain(packageName = "com.dotnomi.fabricdependencyinjection.testclasses.success")
public class SuccessModMain {
  @ModInject
  public ServiceA serviceA;
}
