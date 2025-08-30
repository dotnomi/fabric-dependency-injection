package com.dotnomi.fabricdependencyinjection.java.testclasses.identifier;

import com.dotnomi.fabricdependencyinjection.annotation.ModIdentifier;
import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class DataProcessor {
  private final StorageService fileStorage;

  @ModInject
  @ModIdentifier("database")
  private StorageService databaseStorage;

  @ModInject
  public DataProcessor(@ModIdentifier("file") StorageService fileStorage) {
    this.fileStorage = fileStorage;
  }

  public StorageService getFileStorage() { return fileStorage; }
  public StorageService getDatabaseStorage() { return databaseStorage; }
}
