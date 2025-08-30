package com.dotnomi.fabricdependencyinjection.testclasses.beanlist;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class ChatPlugin implements Plugin {
  @Override
  public String getName() {
    return "Chat";
  }
}
