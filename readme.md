# Fabric Dependency Injection

[](https://github.com/dotnomi/fabricdependencyinjection)
[]([https://opensource.org/licenses/gpl-3-0)
[![](https://jitpack.io/v/dotnomi/fabric-dependency-injection.svg)](https://jitpack.io/#dotnomi/fabric-dependency-injection)

A slim, lightweight, and thread-safe dependency injection (DI) framework originally designed for Fabric mods. It enables clean code organization and decoupled components by automatically managing object instances and their dependencies.

## Table of Contents

- [Features](#features)
- [Compatibility](#compatibility)
- [Core Concepts](#core-concepts)
    - [`@ModScoped`](#modscoped)
    - [`@ModInject`](#modinject)
    - [`@ModMain`](#modmain)
- [Getting Started](#getting-started)
    - [1. Installation](#1-installation)
    - [2. Defining a Service Interface](#2-defining-a-service-interface)
    - [3. Creating an Implementation](#3-creating-an-implementation)
    - [4. Injecting the Dependency](#4-injecting-the-dependency)
    - [5. Initializing the Injector](#5-initializing-the-injector)
- [Accessing Beans Manually](#accessing-beans-manually)
  - [Getting a Single Instance](#getting-a-single-instance)
  - [Getting all Instances of a Type](#getting-all-instances-of-a-type)
- [Advanced Dependency Injection](#advanced-dependency-injection)
    - [Injecting a List of Beans (`BeanList<T>`)](#injecting-a-list-of-beans-beanlistt)
    - [Qualifying Beans with `@ModIdentifier`](#qualifying-beans-with-modidentifier)
    - [Bean Lifecycle with `@PostConstruct`](#bean-lifecycle-with-postconstruct)
- [How It Works](#how-it-works)
- [Error Handling](#error-handling)
- [License](#license)

-----

## Features

- **Mod-Specific Containers**: Each mod gets its own isolated DI container to prevent conflicts.
- **Annotation-Driven**: Configure your dependencies declaratively using simple annotations.
- **Constructor & Field Injection**: Supports the two most common types of dependency injection.
- **Full Integration of Main Class**: Your main mod class (annotated with `@ModMain`) is fully integrated, allowing direct field injection with `@ModInject`.
- **Singleton Scope**: All classes annotated with `@ModScoped` are managed as singletons within their mod's container.
- **List Injection**: Inject all implementations of a specific interface into a single list.
- **Qualifiers (`@ModIdentifier`)**: Distinguish between multiple implementations of the same interface.
- **Lifecycle Management (`@PostConstruct`)**: Execute initialization logic after all dependencies have been injected.
- **Automatic Circular Dependency Detection**: Prevents stack overflow errors at runtime by robustly detecting cycles.
- **Eager Instantiation**: All managed classes are initialized at mod startup, catching configuration errors early.
- **Thread-Safe**: Designed for safe use in multi-threaded environments.

-----

## Compatibility

⚠️ **Disclaimer:** This framework is primarily developed and tested for the **Fabric** modding platform. Therefore, functionality is only guaranteed for use with Fabric.

However, since the framework has **no direct dependencies** on the Fabric API, it is theoretically platform-agnostic and should work with other mod loaders (like Forge, NeoForge, etc.). Use on platforms other than Fabric is at your own risk.

-----

## Core Concepts

The framework is built around three central annotations and the concept of a mod-specific container.

### `@ModScoped`

This annotation marks a class as a managed component (also known as a "bean" or "service"). Any class annotated with `@ModScoped` will be instantiated as a singleton by the `ModInjector` and stored in the mod's DI container.

### `@ModInject`

This annotation tells the `ModInjector` where to inject dependencies. It can be used in two ways:

1.  **Constructor Injection**: Place it on a constructor. The injector will use this constructor to create the class instance, automatically providing instances for all parameters.
2.  **Field Injection**: Place it on a field. The injector will inject a suitable instance into this field after the object is created.

### `@ModMain`

This annotation marks your mod's main class. It serves as the starting point for the classpath scan and allows the class itself to be treated as a managed bean.

-----

## Getting Started

This tutorial demonstrates how to create a service and inject it into a manager using best practices (with interfaces).

### 1\. Installation

Add the dependency to your `build.gradle` file.

**`build.gradle`**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // Replace 'Tag' with the latest version from the JitPack badge above
    implementation 'com.github.dotnomi:fabric-dependency-injection:Tag'
}
```

### 2\. Defining a Service Interface

Instead of working with concrete classes, we define a contract (an interface). This makes your code more flexible and easier to test.

**`MessageService.java`**

```java
package com.mymod.service;

public interface MessageService {
    String getWelcomeMessage(String playerName);
}
```

### 3\. Creating an Implementation

Now, create a class that implements this interface and mark it with `@ModScoped` so the injector can manage it.

**`ConfigMessageService.java`**

```java
package com.mymod.service.impl;

import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.mymod.service.MessageService;

@ModScoped
public class ConfigMessageService implements MessageService {
    @Override
    public String getWelcomeMessage(String playerName) {
        // In a real application, this would come from a config file
        return "Welcome, " + playerName + "!";
    }
}
```

### 4\. Injecting the Dependency

Create a `PlayerManager` class that depends on `MessageService`. Inject the interface (not the concrete class) via the constructor.

**`PlayerManager.java`**

```java
package com.mymod.manager;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.mymod.service.MessageService;

@ModScoped
public class PlayerManager {

    private final MessageService messageService;

    // The injector will automatically find the ConfigMessageService implementation
    // and provide it here.
    @ModInject
    public PlayerManager(MessageService messageService) {
        this.messageService = messageService;
    }

    public void onPlayerJoin(String playerName) {
        String welcomeMessage = messageService.getWelcomeMessage(playerName);
        System.out.println(welcomeMessage);
    }
}
```

### 5\. Initializing the Injector

In your main mod class, you can now directly inject dependencies into fields. Call `ModInjector.initialize()` in your `onInitialize` method, passing `this` to integrate the main class instance into the container.

**`MyMod.java`**

```java
package com.mymod;

import com.dotnomi.fabricdependencyinjection.ModInjector;
import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModMain;
import com.mymod.manager.PlayerManager;
import net.fabricmc.api.ModInitializer;

@ModMain
public class MyMod implements ModInitializer {

    public static final String MOD_ID = "mymod";
    
    // This field will be automatically populated by the injector!
    @ModInject
    private PlayerManager playerManager;

    @Override
    public void onInitialize() {
        // Pass 'this' (the instance) to initialize the container.
        // After this line returns, the 'playerManager' field will be injected.
        ModInjector.initialize(MOD_ID, this);

        // The playerManager is now ready to use directly. No need for manual lookups.
        playerManager.onPlayerJoin("Steve");
    }
}
```

-----

## Accessing Beans Manually

While automatic injection is preferred, you sometimes need to access a managed bean from a location where injection is not possible (e.g., in static methods, vanilla classes, or integration points with other mods). The `ModInjector` provides static methods for this purpose.

### Getting a Single Instance

Use `ModInjector.getInstanceOf()` to retrieve a single bean instance.

```java
// Somewhere in your code, e.g., a static helper method
public class SomeUtil {
    public static void doSomethingWithPlayerManager() {
        // Retrieve the PlayerManager instance from the container
        PlayerManager manager = ModInjector.getInstanceOf(MyMod.MOD_ID, PlayerManager.class);
        manager.onPlayerJoin("Alex");
    }
}
```

If you have multiple implementations of an interface, you can specify which one you need using an identifier.

```java
// Assuming a StorageService with a @ModIdentifier("database")
StorageService dbService = ModInjector.getInstanceOf(MyMod.MOD_ID, StorageService.class, "database");
```

### Getting All Instances of a Type

Use `ModInjector.getInstancesOf()` to get a `BeanList` containing all beans that implement a specific interface. This is useful for plugin- or listener-style systems.

```java
public class EventBroadcaster {
    public void broadcastPlayerJoinEvent(PlayerEntity player) {
        // Get all registered listeners
        BeanList<PlayerJoinListener> listeners = ModInjector.getInstancesOf(MyMod.MOD_ID, PlayerJoinListener.class);

        // Notify each listener
        for (PlayerJoinListener listener : listeners) {
            listener.onPlayerJoin(player);
        }
    }
}
```

-----

## Advanced Dependency Injection

### Injecting a List of Beans (`BeanList<T>`)

Sometimes you want to get all implementations of a specific interface, for example, for a command or plugin system. For this, you can use `BeanList<T>`.

**Example:** Registering multiple chat commands.

1.  **Define a `ChatCommand` interface:**

    ```java
    public interface ChatCommand { void execute(); }
    ```

2.  **Create multiple implementations:**

    ```java
    @ModScoped
    public class HelpCommand implements ChatCommand { /* ... */ }

    @ModScoped
    public class StatusCommand implements ChatCommand { /* ... */ }
    ```

3.  **Inject the list of all commands:**

    ```java
    @ModScoped
    public class CommandManager {
        private final BeanList<ChatCommand> commands;

        @ModInject
        public CommandManager(BeanList<ChatCommand> commands) {
            this.commands = commands; // Contains instances of HelpCommand and StatusCommand
        }

        public void registerAll() {
            // ...
        }
    }
    ```

### Qualifying Beans with `@ModIdentifier`

If there are multiple implementations for the same interface, you need to tell the injector which one to use. Use `@ModIdentifier` to give each implementation a unique name.

**Example:** Selecting a specific storage service.

1.  **Define a `StorageService` interface and two implementations:**

    ```java
    public interface StorageService { void saveData(String data); }

    @ModScoped
    @ModIdentifier("file")
    public class FileStorage implements StorageService { /* ... */ }

    @ModScoped
    @ModIdentifier("database")
    public class DatabaseStorage implements StorageService { /* ... */ }
    ```

2.  **Inject a specific implementation:**

    ```java
    @ModScoped
    public class PlayerDataHandler {
        private final StorageService storage;

        @ModInject
        public PlayerDataHandler(@ModIdentifier("file") StorageService storage) {
            // The FileStorage instance is guaranteed to be injected here.
            this.storage = storage;
        }
    }
    ```

### Bean Lifecycle with `@PostConstruct`

If a class needs to run initialization logic after its dependencies have been injected, you can annotate a method with `@PostConstruct`. This also works in your `@ModMain` class.

**Example:** Loading a configuration file.

```java
@ModScoped
public class ConfigService {
    
    @ModInject
    private ModPaths modPaths;
    private Configuration config;

    // This method is called AFTER modPaths has been injected.
    @PostConstruct
    public void load() {
        // Load the configuration from the disk...
        System.out.println("Configuration loaded!");
    }
}
```

-----

## How It Works

1.  `ModInjector.initialize(modId, mainInstance)` is called.
2.  The provided `@ModMain` instance is immediately registered as a bean in the container.
3.  A new, dedicated `ModContainer` is created for the `modId`.
4.  The `@ModMain` annotation is read to determine the base package for scanning.
5.  Using the `Reflections` library, the classpath is scanned for all classes annotated with `@ModScoped`.
6.  The container creates an instance for each found `@ModScoped` class, recursively resolving its dependencies.
7.  Finally, dependencies are injected into the fields of the registered `@ModMain` instance, and its `@PostConstruct` method is invoked.
8.  The container is now fully initialized and running.

-----

## Error Handling

The framework throws specific exceptions to clearly identify configuration and runtime problems:

- `ContainerAlreadyInitializedException`: Thrown if `initialize()` is called more than once for the same `modId`.
- `ContainerNotInitializedException`: Thrown if `getInstanceOf()` is called before the container has been initialized.
- `CircularDependencyException`: Thrown when a circular dependency is detected.
- `MultipleInjectableConstructorsException`: Thrown if a class has more than one constructor annotated with `@ModInject`.
- `NoInjectableConstructorException`: Thrown if no suitable constructor is found.
- `TooManyInstancesFoundException`: Thrown when requesting a single instance of an interface that has multiple implementations without specifying an `@ModIdentifier`.
- `UnmanagedClassException`: Thrown when an instance of a class not managed by the container is requested.
- `NoMainClassException`: Thrown if the class passed to `initialize()` is not annotated with `@ModMain`.
- `InstanceCreationException`: A general-purpose error for when instantiation fails for any other reason.

-----

## License

This project is licensed under the GPL-3.0 License. See the [LICENSE](https://github.com/dotnomi/fabric-dependency-injection?tab=GPL-3.0-1-ov-file) file for details.