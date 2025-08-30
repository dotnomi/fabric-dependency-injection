package com.dotnomi.fabricdependencyinjection.java.testclasses.postconstruct.failing;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct;

@ModScoped
public class FailingPostConstructService {
  @PostConstruct
  private void initialize() {
    throw new RuntimeException("Initialization failed!");
  }
}
