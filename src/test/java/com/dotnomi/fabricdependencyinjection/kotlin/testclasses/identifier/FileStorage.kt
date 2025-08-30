package com.dotnomi.fabricdependencyinjection.kotlin.testclasses.identifier

import com.dotnomi.fabricdependencyinjection.annotation.ModIdentifier
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped

@ModScoped
@ModIdentifier("file")
class FileStorage : StorageService {
    override fun store(data: String): String {
        return "Saved to file"
    }
}
