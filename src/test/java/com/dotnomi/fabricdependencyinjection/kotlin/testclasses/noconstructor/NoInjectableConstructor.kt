package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.noconstructor

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class NoInjectableConstructor private constructor()
