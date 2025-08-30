package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.inheritance

import com.dotnomi.fabricdependencyinjection.annotation.ModInject

open class BaseService {
    @ModInject
    lateinit var serviceB: ServiceB
}
