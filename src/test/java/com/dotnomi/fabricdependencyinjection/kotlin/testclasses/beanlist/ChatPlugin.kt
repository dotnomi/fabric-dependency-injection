package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.beanlist

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class ChatPlugin() : Plugin {
    override fun getName(): String {
        return "Chat"
    }
}
