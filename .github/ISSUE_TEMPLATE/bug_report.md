---
name: 🐞 Bug Report
about: Create a report to help us improve the framework's stability.
title: "[Bug] "
labels: bug, needs-triage
assignees: ''

---

### 1. Describe the bug

### 2. Minimal Reproducible Example

```java
// Paste the minimal, self-contained code that reproduces the bug here.

// Example of a good reproducible example for a circular dependency issue:
// --- com/example/MyMod.java
package com.example;

import com.dotnomi.fabricdependencyinjection.ModInjector;
import com.dotnomi.fabricdependencyinjection.annotation.ModMain;

public class MyMod {
    public static final String MOD_ID = "mymod";

    public void onInitialize() {
        // This call throws CircularDependencyException
        ModInjector.initialize(MOD_ID, MainConfig.class);
    }
}

// --- com/example/MainConfig.java
package com.example;

import com.dotnomi.fabricdependencyinjection.annotation.ModMain;

@ModMain
public class MainConfig {}


// --- com/example/ServiceA.java
package com.example.services;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class ServiceA {
    @ModInject
    public ServiceA(ServiceB serviceB) { }
}

// --- com.example/ServiceB.java
package com.example.services;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;

@ModScoped
public class ServiceB {
    // This creates the circular dependency: A -> B -> A
    @ModInject
    public ServiceB(ServiceA serviceA) { }
}
````
### 3. Expected behavior

### 4. Environment (please complete the following information)

- **Fabric DI Version:** [e.g., 1.1.0]
- **Minecraft Version:** [e.g., 1.20.4]
- **Fabric Loader Version:** [e.g., 0.15.7]
- **Fabric API Version:** [e.g., 0.91.1+1.20.4]
- **Java Version:** [e.g., OpenJDK 17.0.9]
- **Operating System:** [e.g., Windows 11, Ubuntu 22.04]

### 5. Logs & Stack Traces

```log
// Paste the full stack trace here
```

### 6. Additional context