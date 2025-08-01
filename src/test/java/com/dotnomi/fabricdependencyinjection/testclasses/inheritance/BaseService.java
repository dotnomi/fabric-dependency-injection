package com.dotnomi.fabricdependencyinjection.testclasses.inheritance;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;

public class BaseService {
  @ModInject
  protected ServiceB serviceB;
}
