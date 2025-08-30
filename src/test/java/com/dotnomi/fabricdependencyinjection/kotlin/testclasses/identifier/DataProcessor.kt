package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.identifier

import com.dotnomi.fabricdependencyinjection.annotation.ModIdentifier
import com.dotnomi.fabricdependencyinjection.annotation.ModInject
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
class DataProcessor @ModInject constructor(@param:ModIdentifier("file") val fileStorage: StorageService) {
    @ModInject
    @ModIdentifier("database")
    lateinit var databaseStorage: StorageService
}
