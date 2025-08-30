package com.dotnomi.fabricdependencyinjection.java.testclasses.success;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModMain;

@ModMain
public class SuccessModMain {
  @ModInject
  public ServiceA serviceA;
}
