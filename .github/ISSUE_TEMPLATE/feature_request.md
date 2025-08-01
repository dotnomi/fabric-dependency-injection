---
name: ✨ Feature Request
about: Suggest a new idea or an enhancement for the framework.
title: "[Feature] "
labels: enhancement, needs-discussion
assignees: ''

---

### 1. Is your feature request related to a problem? Please describe

### 2. Describe the solution you'd like

### 3. Describe alternatives you've considered

### 4. Proposed API (Optional, but highly recommended)

```java
// Your code example here.
// For example, showing a new annotation, a new method in `ModInjector`, or a new configuration option.

@ModScoped
public class SomeManager {
    
    // Example: A new annotation for lazy injection
    @ModInject
    @Lazy
    private Provider<MyHeavyService> heavyServiceProvider;
    
    public void doWork() {
        // The service is only instantiated when .get() is called
        MyHeavyService instance = heavyServiceProvider.get();
        instance.run();
    }
}
```

### 5. Additional Context
