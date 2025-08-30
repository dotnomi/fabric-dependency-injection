package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.success

import com.dotnomi.fabricdependencyinjection.annotation.ModInject
import com.dotnomi.fabricdependencyinjection.annotation.ModMain

@ModMain
class SuccessModMain {
    @ModInject
    lateinit var serviceA: ServiceA
}
