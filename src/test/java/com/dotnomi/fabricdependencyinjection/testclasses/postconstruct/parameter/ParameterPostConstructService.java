package com.dotnomi.fabricdependencyinjection.testclasses.postconstruct.parameter;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct;

@ModScoped
@SuppressWarnings("unused")
public class ParameterPostConstructService {
  @PostConstruct
  private void initialize(String var1) {}
}
