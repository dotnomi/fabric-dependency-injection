package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.success

import com.dotnomi.fabricdependencyinjection.annotation.ModInject
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class ServiceA @ModInject constructor(val serviceB: ServiceB)
