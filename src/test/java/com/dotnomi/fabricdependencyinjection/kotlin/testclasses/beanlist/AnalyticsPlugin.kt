package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.beanlist

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class AnalyticsPlugin() : Plugin {
    override fun getName(): String {
        return "Analytics"
    }
}
