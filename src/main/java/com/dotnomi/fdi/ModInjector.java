package com.dotnomi.ddi;

import com.dotnomi.ddi.annotation.ModInject;
import com.dotnomi.ddi.annotation.ModMain;
import com.dotnomi.ddi.annotation.ModScoped;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple, thread-safe dependency injection container.
 * The injector is initialized once and provides managed instances of classes
 * annotated with {@link ModScoped}
 */
public final class ModInjector {
  private static final ModContainer modContainer = new ModContainer();

  /**
   * Initializes the dependency injection container. This method scans the classpath
   * for manageable classes and pre-instantiates them. It must be called once
   * at the beginning of the mod lifecycle.
   *
   * @param mainClass The main class of the mod, annotated with {@link ModMain}.
   *                  This class defines the base package for the classpath scanning.
   * @throws IllegalStateException if the container has already been initialized.
   * @throws IllegalArgumentException if the main class is not annotated with {@link ModMain}
   */
  public static void initialize(Class<?> mainClass, String modId) throws IllegalStateException, IllegalArgumentException {
    modContainer.initialize(mainClass);
  }

  /**
   * Retrieves a managed instance of the specified class from the container.
   *
   * @param targetClass The class type of the instance to retrieve.
   * @param <T> The type of the class.
   * @return The singleton instance of the requested class.
   * @throws IllegalStateException if the container has not been initialized yet.
   * @throws IllegalArgumentException if the requested class is not managed by the injector
   *                                  (i.e., not annotated with {@link ModScoped}).
   */
  public static <T> T getInstanceOf(Class<T> targetClass) throws IllegalStateException, IllegalArgumentException {
    return modContainer.getInstanceOf(targetClass);
  }

  /**
   * The internal, stateful container that manages the DI lifecycle.
   * This class is not intended for direct use.
   */
  private static final class ModContainer {
    private static final Logger logger = LoggerFactory.getLogger(ModContainer.class);

    private final Map<Class<?>, Object> modScopedInstances = new ConcurrentHashMap<>();
    private final ThreadLocal<Set<Class<?>>> instanceCache = ThreadLocal.withInitial(HashSet::new);

    private volatile boolean initialized = false;

    public synchronized void initialize(Class<?> mainClass) throws IllegalStateException, IllegalArgumentException {
      if (this.initialized) {
        throw new IllegalStateException("ModContainer already initialized.");
      }

      logger.debug("Starting container initialization...");

      if (!mainClass.isAnnotationPresent(ModMain.class)) {
        throw new IllegalArgumentException("Provided class " + mainClass.getName() + " is not annotated with @ModMain.");
      }

      ModMain modMainAnnotation = mainClass.getAnnotation(ModMain.class);
      String basePackage = modMainAnnotation.packageName();

      if (basePackage.isEmpty()) {
        basePackage = mainClass.getPackage().getName();
        logger.info("No custom package specified in @ModMain. Using package of @ModMain class: {}", basePackage);
      } else {
        logger.info("Using custom package specified in @ModMain: {}", basePackage);
      }

      logger.info("Starting classpath scanning for package: {}", basePackage);
      Reflections reflections = new Reflections(basePackage);
      Set<Class<?>> modScopedClasses = reflections.getTypesAnnotatedWith(ModScoped.class);
      logger.debug("Found {} @ModScoped classes to manage: {}", modScopedClasses.size(), modScopedClasses);

      // Annotations cannot be instantiated
      modScopedClasses.remove(ModMain.class);
      this.initialized = true;

      // Pre-instantiate all found classes to detect errors early
      for (Class<?> modScopedClass : modScopedClasses) {
        try {
          getInstanceOf(modScopedClass);
        } catch (Exception exception) {
          logger.error("Failed to pre-instantiate or register class {} for DI: {}", modScopedClass.getName(), exception.getMessage());
          this.initialized = false;
          throw new RuntimeException("DI Initialization failed for " + modScopedClass.getName(), exception);
        }
      }

      logger.info("Dotnomi ModInjector initialized successfully.");
    }

    public <T> T getInstanceOf(Class<T> targetClass) {
      logger.debug("Request for instance of type [{}].", targetClass.getName());

      if (!this.initialized) {
        throw new IllegalStateException("ModContainer has not been initialized yet. Call ModInjector.initialize() first.");
      }

      if (!targetClass.isAnnotationPresent(ModScoped.class) && !targetClass.isAnnotationPresent(ModMain.class)) {
        throw new IllegalArgumentException("Class " + targetClass.getName() + " is not annotated with @ModScoped and cannot be managed by the injector.");
      }

      // Double-Checked Locking Pattern for thread-safe lazy initialization
      Object instance = modScopedInstances.get(targetClass);
      if (instance == null) {
        synchronized (this) {
          instance = modScopedInstances.get(targetClass);
          if (instance == null) {
            logger.debug("Cache miss for [{}]. Proceeding to create new instance.", targetClass.getName());
            instance = createInstance(targetClass);
            modScopedInstances.put(targetClass, instance);
          } else {
            logger.debug("Cache hit for [{}] after acquiring lock.", targetClass.getName());
          }
        }
      } else {
        logger.debug("Cache hit for [{}]. Returning existing instance.", targetClass.getName());
      }

      return targetClass.cast(instance);
    }

    private <T> T createInstance(Class<T> targetClass) {
      if (instanceCache.get().contains(targetClass)) {
        logger.error("Circular dependency detected! Path includes class: {}", targetClass.getName());
        throw new IllegalStateException("Circular dependency detected for class " + targetClass.getName());
      }

      try {
        instanceCache.get().add(targetClass);
        logger.debug("Creating new instance of [{}].", targetClass.getName());

        Constructor<?> injectableConstructor = findInjectableConstructor(targetClass);
        logger.debug("Using constructor [{}] for class [{}].", injectableConstructor, targetClass.getName());

        Object[] args = new Object[injectableConstructor.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
          Class<?> paramType = injectableConstructor.getParameterTypes()[i];
          logger.debug("Resolving constructor dependency of type [{}] for [{}].", paramType.getName(), targetClass.getName());
          args[i] = getInstanceOf(paramType);
        }

        T newInstance = targetClass.cast(injectableConstructor.newInstance(args));
        logger.debug("Successfully instantiated [{}]. Proceeding with field injection.", targetClass.getName());

        injectFields(newInstance, targetClass);
        return newInstance;
      } catch (Exception exception) {
        logger.error("Failed to create an instance of class [{}].", targetClass.getName(), exception);
        throw new RuntimeException("Failed to create instance of " + targetClass.getName(), exception);
      } finally {
        // Always remove the class from the cache after a creation attempt
        instanceCache.get().remove(targetClass);
      }
    }

    private void injectFields(Object instance, Class<?> targetClass) throws IllegalAccessException {
      Class<?> currentClass = targetClass;
      while (currentClass != null && currentClass != Object.class) {
        for (Field field : currentClass.getDeclaredFields()) {
          if (field.isAnnotationPresent(ModInject.class)) {
            logger.debug("Found @ModInject on field [{}] in class [{}].", field.getName(), targetClass.getName());
            field.setAccessible(true);
            Object dependency = getInstanceOf(field.getType());
            field.set(instance, dependency);
            logger.debug("Injected dependency of type [{}] into field [{}].", dependency.getClass().getName(), field.getName());
          }
        }
        currentClass = currentClass.getSuperclass();
      }
    }

    private <T> Constructor<?> findInjectableConstructor(Class<T> targetClass) {
      List<Constructor<?>> injectableConstructors = new ArrayList<>();
      for (Constructor<?> constructor : targetClass.getConstructors()) {
        if (constructor.isAnnotationPresent(ModInject.class)
          || Arrays.stream(constructor.getParameters()).anyMatch(p -> p.isAnnotationPresent(ModInject.class))
        ) {
          injectableConstructors.add(constructor);
        }
      }

      if (injectableConstructors.size() > 1) {
        throw new IllegalStateException("Multiple injectable constructors found for class " + targetClass.getName());
      }

      if (!injectableConstructors.isEmpty()) {
        return injectableConstructors.getFirst();
      }

      try {
        return targetClass.getConstructor();
      } catch (NoSuchMethodException exception) {
        throw new IllegalStateException("No injectable constructors found for class " + targetClass.getName(), exception);
      }
    }
  }
}
