package com.dotnomi.fabricdependencyinjection.testclasses.identifier;

import com.dotnomi.fabricdependencyinjection.annotation.ModIdentifier;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
@ModIdentifier("database")
public class DatabaseStorage implements StorageService {
  @Override
  public String store(String data) {
    return "Saved to database";
  }
}
