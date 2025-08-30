package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.postconstruct.parameter

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct

@ModScoped
class ParameterPostConstructService {
    @PostConstruct
    private fun initialize(var1: String) {
    }
}
