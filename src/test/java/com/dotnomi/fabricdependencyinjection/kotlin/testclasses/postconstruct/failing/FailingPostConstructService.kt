package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.postconstruct.failing

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct

@ModScoped
class FailingPostConstructService {
    @PostConstruct
    private fun initialize() {
        throw RuntimeException("Initialization failed!")
    }
}
