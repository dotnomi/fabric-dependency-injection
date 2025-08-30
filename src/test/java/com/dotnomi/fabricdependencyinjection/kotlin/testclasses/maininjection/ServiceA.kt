package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.maininjection

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class ServiceA {
    fun sayHello(): String {
        return "Hello from ServiceA"
    }
}
