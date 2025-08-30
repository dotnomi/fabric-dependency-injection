package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.identifier

import com.dotnomi.fabricdependencyinjection.annotation.ModIdentifier
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
@ModIdentifier("database")
class DatabaseStorage : StorageService {
    override fun store(data: String): String {
        return "Saved to database"
    }
}
