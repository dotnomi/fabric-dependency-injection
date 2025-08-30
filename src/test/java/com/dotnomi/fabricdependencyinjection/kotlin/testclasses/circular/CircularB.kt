package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.circular

import com.dotnomi.fabricdependencyinjection.annotation.ModInject
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class CircularB @ModInject constructor(circularA: CircularA)
