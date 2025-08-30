package com.dotnomi.fabricdependencyinjection.testclasses.identifier;

import com.dotnomi.fabricdependencyinjection.annotation.ModIdentifier;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
@ModIdentifier("file")
public class FileStorage implements StorageService {
  @Override
  public String store(String data) {
    return "Saved to file";
  }
}
