package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.postconstruct.multiple

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct

@ModScoped
class MultiplePostConstructService {
    var isFirstBoolean: Boolean = false
        private set
    var isSecondBoolean: Boolean = false
        private set

    @PostConstruct
    private fun firstInitialize() {
        this.isFirstBoolean = true
    }

    @PostConstruct
    private fun secondInitialize() {
        this.isSecondBoolean = true
    }
}
