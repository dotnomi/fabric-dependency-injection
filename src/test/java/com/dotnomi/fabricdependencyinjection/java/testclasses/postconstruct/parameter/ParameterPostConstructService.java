package com.dotnomi.fabricdependencyinjection.java.testclasses.postconstruct.parameter;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct;

@ModScoped
public class ParameterPostConstructService {
  @PostConstruct
  private void initialize(String var1) {}
}
