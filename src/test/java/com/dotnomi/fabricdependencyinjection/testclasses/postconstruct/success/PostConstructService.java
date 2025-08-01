package com.dotnomi.fabricdependencyinjection.testclasses.postconstruct.success;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct;

@ModScoped
@SuppressWarnings("unused")
public class PostConstructService {
  private boolean initialized;

  @PostConstruct
  private void initialize() {
    this.initialized = true;
  }

  public boolean isInitialized() {
    return this.initialized;
  }
}
