package com.dotnomi.fabricdependencyinjection.java.testclasses.beanlist;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class AnalyticsPlugin implements Plugin {
  @Override
  public String getName() {
    return "Analytics";
  }
}
