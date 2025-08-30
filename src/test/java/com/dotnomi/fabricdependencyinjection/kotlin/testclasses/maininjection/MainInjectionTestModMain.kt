package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.maininjection

import com.dotnomi.fabricdependencyinjection.annotation.ModInject
import com.dotnomi.fabricdependencyinjection.annotation.ModMain
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct

@ModMain
class MainInjectionTestModMain {
    @ModInject
    lateinit var serviceA: ServiceA

    var isPostConstructCalled: Boolean = false
        private set

    @PostConstruct
    private fun initialize() {
        this.isPostConstructCalled = true
    }
}
