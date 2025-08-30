package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.beanlist

import com.dotnomi.fabricdependencyinjection.BeanList
import com.dotnomi.fabricdependencyinjection.annotation.ModInject
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class PluginManager @ModInject constructor(val constructorInjectedPlugins: BeanList<Plugin>) {
    @ModInject
    lateinit var fieldInjectedPlugins: BeanList<Plugin>
}
