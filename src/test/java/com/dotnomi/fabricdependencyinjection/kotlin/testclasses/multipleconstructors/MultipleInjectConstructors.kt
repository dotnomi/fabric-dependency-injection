package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.multipleconstructors

import com.dotnomi.fabricdependencyinjection.annotation.ModInject
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class MultipleInjectConstructors {
    @ModInject
    constructor(serviceA: ServiceA)

    @ModInject
    constructor(serviceA: ServiceA, serviceB: ServiceB)
}
