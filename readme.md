# Fabric Dependency Injection

[](https://github.com/dotnomi/fabricdependencyinjection)
[](https://opensource.org/licenses/MIT)

A simple, lightweight, and thread-safe dependency injection (DI) framework designed specifically for Fabric mods. It enables clean code organization and decoupled components by automatically managing object instances and their dependencies.

## Table of Contents

- [Features](#features)
- [Core Concepts](#core-concepts)
    - [`@ModScoped`](#modscoped)
    - [`@ModInject`](#modinject)
    - [`@ModMain`](#modmain)
    - [Mod Containers](#mod-containers)
- [Getting Started](#getting-started)
    - [1. Installation](#1-installation)
    - [2. Defining Components](#2-defining-components)
    - [3. Injecting Dependencies](#3-injecting-dependencies)
    - [4. Initializing the Injector](#4-initializing-the-injector)
    - [5. Retrieving Instances (Optional)](#5-retrieving-instances-optional)
- [How It Works](#how-it-works)
- [Error Handling](#error-handling)
- [License](#license)

-----

## Features

- **Mod-Specific Containers**: Each mod gets its own isolated DI container to prevent conflicts.
- **Annotation-Driven**: Configure your dependencies declaratively using simple annotations.
- **Constructor & Field Injection**: Supports the two most common types of dependency injection.
- **Singleton Scope**: All classes annotated with `@ModScoped` are managed as singletons within their mod's container.
- **Automatic Circular Dependency Detection**: Prevents stack overflow errors at runtime by robustly detecting cycles.
- **Eager Instantiation**: All managed classes are initialized at mod startup, avoiding latency on first use and catching configuration errors early.
- **Thread-Safety**: Designed for safe use in multi-threaded environments.

-----

## Core Concepts

The framework is built around three central annotations and the concept of a mod-specific container.

### `@ModScoped`

This annotation marks a class as a managed component (also known as a "bean" or "service"). Any class annotated with `@ModScoped` will be instantiated as a singleton by the `ModInjector` and stored in the mod's DI container. Only `@ModScoped` classes can be injected or have dependencies injected into them.

```java
package com.mymod.service;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class MyService {
    public void doSomething() {
        System.out.println("Service was called!");
    }
}
```

### `@ModInject`

This annotation tells the `ModInjector` where to inject dependencies. It can be used in two ways:

1.  **Constructor Injection**: Place it on a constructor. The injector will use this constructor to create the class instance, automatically providing instances for all parameters.
2.  **Field Injection**: Place it on a field. After the object is instantiated, the injector will inject a suitable instance into this field.

<!-- end list -->

```java
package com.mymod.manager;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.mymod.service.MyService;

@ModScoped
public class MyManager {

    private final MyService service;

    // Constructor Injection
    @ModInject
    public MyManager(MyService service) {
        this.service = service;
    }

    public void manage() {
        service.doSomething();
    }
}
```

### `@ModMain`

This annotation marks your mod's main class. It serves as the starting point for the classpath scan. The `ModInjector` will scan the package (and all sub-packages) of this class for any classes annotated with `@ModScoped`.

```java
package com.mymod;

import com.dotnomi.fabricdependencyinjection.annotation.ModMain;
import net.fabricmc.api.ModInitializer;

@ModMain
public class MyMod implements ModInitializer {
    // ...
}
```

Optionally, you can specify a `packageName` to limit the scan to a different base package: `@ModMain(packageName = "com.mymod.components")`.

### Mod Containers

Each mod is identified by a unique **`modId`**. The `ModInjector` creates a separate, isolated container for each `modId`. This ensures that one mod's dependencies do not conflict with another's.

-----

## Getting Started

### 1\. Installation

Add the dependencies to your `build.gradle` file. You need the framework itself and the `org.reflections` library for the classpath scan.

**`build.gradle`**

```groovy
repositories {
    // Add the repository where your framework is hosted
    // e.g., maven { url 'https://jitpack.io' }
}

dependencies {
    // Replace this with the correct notation for your library
    implementation 'com.dotnomi:fabric-dependency-injection:1.0.0'

    // The framework requires 'org.reflections'
    implementation 'org.reflections:reflections:0.10.2'
}
```

### 2\. Defining Components

Create your services, managers, or other components and annotate them with `@ModScoped`.

**`ConfigService.java`**

```java
package com.mymod.service;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class ConfigService {
    public String getGreeting() {
        return "Hello, World!";
    }
}
```

### 3\. Injecting Dependencies

Use `@ModInject` to wire your components together.

**Constructor selection priority:**

1.  The single constructor annotated with `@ModInject`.
2.  If no constructor is annotated: The public, no-argument (default) constructor.

**`PlayerManager.java`**

```java
package com.mymod.manager;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.mymod.service.ConfigService;

@ModScoped
public class PlayerManager {

    private final ConfigService config;

    // The injector will automatically provide an instance of ConfigService.
    @ModInject
    public PlayerManager(ConfigService config) {
        this.config = config;
    }

    public void onPlayerJoin(String playerName) {
        System.out.println(playerName + " joined! Message: " + config.getGreeting());
    }
}
```

### 4\. Initializing the Injector

In the `onInitialize` method of your main mod class, call `ModInjector.initialize()`. This is the most critical step, as it builds the entire DI container.

**`MyMod.java`**

```java
package com.mymod;

import com.dotnomi.fabricdependencyinjection.ModInjector;
import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModMain;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.mymod.manager.PlayerManager;
import net.fabricmc.api.ModInitializer;

@ModMain
public class MyMod implements ModInitializer {

    public static final String MOD_ID = "mymod";
    
    private PlayerManager playerManager;

    @Override
    public void onInitialize() {
        // Initializes the container for this mod and scans for components.
        // This also instantiates PlayerManager and injects it into the field above.
        ModInjector.initialize(MOD_ID, MyMod.class);
        
        // In the main class you can't use field injection or constructor injection.
        playerManager = ModInjector.getInstanceOf(MOD_ID, PlayerManager.class);

        // The playerManager is now ready to use.
        playerManager.onPlayerJoin("Steve");
    }
}
```

### 5\. Retrieving Instances (Optional)

You typically don't need to fetch instances manually, as the magic of DI is that dependencies are injected automatically. However, if you need to access a component from a place not managed by the DI container, you can use `ModInjector.getInstanceOf()`.

```java
// Somewhere in your code
PlayerManager manager = ModInjector.getInstanceOf(MyMod.MOD_ID, PlayerManager.class);
manager.manage();
```

-----

## How It Works

The initialization process is as follows:

1.  `ModInjector.initialize(modId, mainClass)` is called.
2.  A new, dedicated `ModContainer` is created for the given `modId`.
3.  The `@ModMain` annotation on the `mainClass` is read to determine the base package for scanning.
4.  Using the `Reflections` library, the classpath is scanned for all classes annotated with `@ModScoped`.
5.  The container iterates through the list of found classes and attempts to create an instance for each one (**Eager Instantiation**).
6.  When creating an instance, its dependencies (constructor parameters and `@ModInject` fields) are resolved recursively.
7.  The created instances are stored as singletons in the container's `modScopedInstances` cache.
8.  After initialization is complete, the container is ready and can serve instances.

-----

## Error Handling

The framework throws specific exceptions to clearly identify configuration and runtime problems:

- `ContainerAlreadyInitializedException`: Thrown if `initialize()` is called more than once for the same `modId`.
- `ContainerNotInitializedException`: Thrown if `getInstanceOf()` is called before the container has been initialized.
- `CircularDependencyException`: Thrown when a circular dependency is detected (e.g., Class A requires B, and Class B requires A).
- `MultipleInjectableConstructorsException`: Thrown if a class has more than one constructor annotated with `@ModInject`.
- `NoInjectableConstructorException`: Thrown if no suitable constructor strategy is found (neither an `@ModInject` constructor nor a public default constructor).
- `UnmanagedClassException`: Thrown when an instance of a class not annotated with `@ModScoped` is requested.
- `NoMainClassException`: Thrown if the class passed to `initialize()` is not annotated with `@ModMain`.
- `InstanceCreationException`: A general-purpose error for when instantiation fails for any other reason (e.g., an error within the constructor itself).

-----

## License

This project is licensed under the MIT License. See the [LICENSE](https://github.com/dotnomi/fabric-dependency-injection?tab=GPL-3.0-1-ov-file) file for details.
