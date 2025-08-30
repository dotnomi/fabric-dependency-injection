package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.failingconstructor

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class FailingConstructor {
    init {
        throw IllegalStateException("Constructor failed")
    }
}
