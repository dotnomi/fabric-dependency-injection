package com.dotnomi.fabricdependencyinjection.testclasses.beanlist;

import com.dotnomi.fabricdependencyinjection.BeanList;
import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class PluginManager {
  private final BeanList<Plugin> constructorInjectedPlugins;

  @ModInject
  private BeanList<Plugin> fieldInjectedPlugins;

  @ModInject
  public PluginManager(BeanList<Plugin> constructorInjectedPlugins) {
    this.constructorInjectedPlugins = constructorInjectedPlugins;
  }

  public BeanList<Plugin> getConstructorInjectedPlugins() { return constructorInjectedPlugins; }
  public BeanList<Plugin> getFieldInjectedPlugins() { return fieldInjectedPlugins; }
}
