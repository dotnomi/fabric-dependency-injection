package com.dotnomi.fabricdependencyinjection.testclasses.postconstruct.multiple;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct;

@ModScoped
@SuppressWarnings("unused")
public class MultiplePostConstructService {
  private boolean firstBoolean;
  private boolean secondBoolean;

  @PostConstruct
  private void firstInitialize() {
    this.firstBoolean = true;
  }

  @PostConstruct
  private void secondInitialize() {
    this.secondBoolean = true;
  }

  public boolean isFirstBoolean() {
    return this.firstBoolean;
  }

  public boolean isSecondBoolean() {
    return this.secondBoolean;
  }
}
