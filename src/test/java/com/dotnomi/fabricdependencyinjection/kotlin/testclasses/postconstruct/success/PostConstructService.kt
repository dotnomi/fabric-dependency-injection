package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.postconstruct.success

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct

@ModScoped
class PostConstructService {
    var isInitialized: Boolean = false
        private set

    @PostConstruct
    private fun initialize() {
        this.isInitialized = true
    }
}
