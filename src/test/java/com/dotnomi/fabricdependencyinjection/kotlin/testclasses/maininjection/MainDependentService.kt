package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.maininjection

import com.dotnomi.fabricdependencyinjection.annotation.ModInject
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class MainDependentService @ModInject constructor(val mainInstance: MainInjectionTestModMain)
