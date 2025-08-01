package com.dotnomi.fabricdependencyinjection;

import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModMain;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.dotnomi.fabricdependencyinjection.exception.CircularDependencyException;
import com.dotnomi.fabricdependencyinjection.exception.ContainerAlreadyInitializedException;
import com.dotnomi.fabricdependencyinjection.exception.ContainerNotInitializedException;
import com.dotnomi.fabricdependencyinjection.exception.InstanceCreationException;
import com.dotnomi.fabricdependencyinjection.exception.ModInjectorException;
import com.dotnomi.fabricdependencyinjection.exception.MultipleInjectableConstructorsException;
import com.dotnomi.fabricdependencyinjection.exception.NoInjectableConstructorException;
import com.dotnomi.fabricdependencyinjection.exception.NoMainClassException;
import com.dotnomi.fabricdependencyinjection.exception.UnmanagedClassException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple, thread-safe dependency injection container.
 * The injector is initialized once and provides managed instances of classes
 * annotated with {@link ModScoped}
 */
public final class ModInjector {
  private static final Map<String, ModContainer> modContainers = new ConcurrentHashMap<>();

  /**
   * Initializes the dependency injection container. This method scans the classpath
   * for manageable classes and pre-instantiates them. It must be called once
   * at the beginning of the mod lifecycle.
   *
   * @param modId The unique ID of the mod.
   * @param mainClass The main class of the mod, annotated with {@link ModMain}.
   *        This class defines the base package for the classpath scanning.
   * @throws ContainerAlreadyInitializedException if the container has already been initialized.
   * @throws NoMainClassException if the main class is not annotated with {@link ModMain}.
   * @throws UnmanagedClassException if the requested class is not managed by the injector
   *         (i.e., not annotated with {@link ModScoped}).
   * @throws CircularDependencyException if a circular dependency is detected during the instantiation
   *         of the class or one of its dependencies.
   * @throws MultipleInjectableConstructorsException if the target class has multiple constructors
   *         annotated with {@link ModInject}.
   * @throws NoInjectableConstructorException if no suitable constructor (either annotated with {@link ModInject}
   *         or a public no-argument constructor) is found for the target class.
   * @throws InstanceCreationException if an unexpected error occurs during the instantiation of the class,
   *         for example, due to a problem with reflection.
   */
  public static void initialize(String modId, Class<?> mainClass) throws ModInjectorException {
    modContainers.computeIfAbsent(modId, ModContainer::new).initialize(mainClass);
  }

  /**
   * Retrieves a managed instance of the specified class from the container.
   *
   * @param modId The unique ID of the mod whose container should be used.
   * @param targetClass The class type of the instance to retrieve.
   * @param <T> The type of the class.
   * @return The singleton instance of the requested class.
   * @throws ContainerNotInitializedException if the container has not been initialized yet.
   * @throws UnmanagedClassException if the requested class is not managed by the injector
   *         (i.e., not annotated with {@link ModScoped}).
   * @throws CircularDependencyException if a circular dependency is detected during the instantiation
   *         of the class or one of its dependencies.
   * @throws MultipleInjectableConstructorsException if the target class has multiple constructors
   *         annotated with {@link ModInject}.
   * @throws NoInjectableConstructorException if no suitable constructor (either annotated with {@link ModInject}
   *         or a public no-argument constructor) is found for the target class.
   * @throws InstanceCreationException if an unexpected error occurs during the instantiation of the class,
   *         for example, due to a problem with reflection.
   */
  public static <T> T getInstanceOf(String modId, Class<T> targetClass) throws ModInjectorException {
    var modContainer = modContainers.get(modId);
    if (modContainer == null) {
      throw new ContainerNotInitializedException("ModContainer has not been initialized yet. Call ModInjector.initialize() first.");
    }
    return modContainer.getInstanceOf(targetClass);
  }

  /**
   * The internal, stateful container that manages the DI lifecycle.
   * This class is not intended for direct use.
   */
  private static final class ModContainer {
    private static final Logger logger = LoggerFactory.getLogger(ModContainer.class);

    private final Map<Class<?>, Object> modScopedInstances = new ConcurrentHashMap<>();

    /**
     * Stores the current instantiation path for the calling thread.
     * Used to detect circular dependencies. A List is used to preserve the order.
     */
    private final ThreadLocal<List<Class<?>>> dependencyStack = ThreadLocal.withInitial(ArrayList::new);

    private final String modId;

    private volatile boolean initialized = false;

    public ModContainer(String modId) {
      this.modId = modId;
    }

    public synchronized void initialize(Class<?> mainClass) {
      if (this.initialized) {
        throw new ContainerAlreadyInitializedException("Container for modId '" + modId + "' has already been initialized.");
      }

      logger.debug("[{}] Starting container initialization...", modId);

      if (!mainClass.isAnnotationPresent(ModMain.class)) {
        throw new NoMainClassException("Provided class " + mainClass.getName() + " is not annotated with @ModMain.");
      }

      var modMainAnnotation = mainClass.getAnnotation(ModMain.class);
      var basePackage = modMainAnnotation.packageName().isEmpty()
        ? mainClass.getPackage().getName()
        : modMainAnnotation.packageName();

      logger.info("[{}] Starting classpath scanning for package: {}", modId, basePackage);
      var reflections = new Reflections(basePackage);
      var modScopedClasses = reflections.getTypesAnnotatedWith(ModScoped.class);
      logger.debug("[{}] Found {} @ModScoped classes to manage: {}", modId, modScopedClasses.size(), modScopedClasses);

      this.initialized = true;

      for (var modScopedClass : modScopedClasses) {
        if (modScopedClass.isAnnotation()) continue; // Skip annotations
        try {
          getInstanceOf(modScopedClass);
        } catch (Exception exception) {
          logger.error("[{}] Failed to pre-instantiate or register class {} for DI.", modId, modScopedClass.getName(), exception);
          this.initialized = false;
          throw exception;
        }
      }

      logger.info("[{}] ModInjector initialized successfully.", modId);
    }

    public <T> T getInstanceOf(Class<T> targetClass) {
      logger.debug("[{}] Request for instance of type [{}].", modId, targetClass.getName());

      if (!this.initialized) {
        throw new ContainerNotInitializedException("ModContainer has not been initialized yet. Call ModInjector.initialize() first.");
      }

      if (!targetClass.isAnnotationPresent(ModScoped.class) && !targetClass.isAnnotationPresent(ModMain.class)) {
        throw new UnmanagedClassException("Class " + targetClass.getName() + " is not annotated with @ModScoped and cannot be managed by the ModInjector.");
      }

      var instance = modScopedInstances.get(targetClass);
      if (instance == null) {
        synchronized (this) {
          instance = modScopedInstances.get(targetClass);
          if (instance == null) {
            logger.debug("[{}] Cache miss for [{}]. Proceeding to create new instance.", modId, targetClass.getName());
            instance = createInstance(targetClass);
            modScopedInstances.put(targetClass, instance);
          } else {
            logger.debug("[{}] Cache hit for [{}] after acquiring lock.", modId, targetClass.getName());
          }
        }
      } else {
        logger.debug("[{}] Cache hit for [{}]. Returning existing instance.", modId, targetClass.getName());
      }

      return targetClass.cast(instance);
    }

    private <T> T createInstance(Class<T> targetClass) {
      if (dependencyStack.get().contains(targetClass)) {
        var path = new ArrayList<>(dependencyStack.get());
        path.add(targetClass);
        throw new CircularDependencyException(path);
      }

      try {
        dependencyStack.get().add(targetClass);
        logger.debug("[{}] Creating new instance of [{}].", modId, targetClass.getName());

        var injectableConstructor = findInjectableConstructor(targetClass);
        logger.debug("[{}] Using constructor [{}] for class [{}].", modId, injectableConstructor, targetClass.getName());

        var args = new Object[injectableConstructor.getParameterCount()];
        for (var i = 0; i < args.length; i++) {
          var paramType = injectableConstructor.getParameterTypes()[i];
          logger.debug("[{}] Resolving constructor dependency of type [{}] for [{}].", modId, paramType.getName(), targetClass.getName());
          args[i] = getInstanceOf(paramType);
        }

        var newInstance = targetClass.cast(injectableConstructor.newInstance(args));
        logger.debug("[{}] Successfully instantiated [{}]. Proceeding with field injection.", modId, targetClass.getName());

        injectFields(newInstance, targetClass);
        return newInstance;
      } catch (Exception exception) {
        if (exception instanceof ModInjectorException modInjectorException) {
          throw modInjectorException;
        }

        logger.error("[{}] Failed to create an instance of class [{}].", modId, targetClass.getName(), exception);
        throw new InstanceCreationException("Failed to create instance of " + targetClass.getName(), exception);
      } finally {
        if (!dependencyStack.get().isEmpty()) {
          dependencyStack.get().removeLast();
        }
      }
    }

    private void injectFields(Object instance, Class<?> targetClass) throws IllegalAccessException {
      var currentClass = targetClass;
      while (currentClass != null && currentClass != Object.class) {
        for (var field : currentClass.getDeclaredFields()) {
          if (field.isAnnotationPresent(ModInject.class)) {
            logger.debug("[{}] Found @ModInject on field [{}] in class [{}].", modId, field.getName(), targetClass.getName());
            field.setAccessible(true);
            var dependency = getInstanceOf(field.getType());
            field.set(instance, dependency);
            logger.debug("[{}] Injected dependency of type [{}] into field [{}].", modId, dependency.getClass().getName(), field.getName());
          }
        }
        currentClass = currentClass.getSuperclass();
      }
    }

    private <T> Constructor<?> findInjectableConstructor(Class<T> targetClass) {
      var injectableConstructors = Arrays.stream(targetClass.getConstructors())
        .filter(constructor -> constructor.isAnnotationPresent(ModInject.class))
        .toList();

      if (injectableConstructors.size() > 1) {
        throw new MultipleInjectableConstructorsException("Multiple constructors annotated with @ModInject found for class " + targetClass.getName());
      }

      if (!injectableConstructors.isEmpty()) {
        return injectableConstructors.getFirst();
      }

      try {
        return targetClass.getConstructor();
      } catch (NoSuchMethodException exception) {
        throw new NoInjectableConstructorException("No default (no-arg) or @ModInject annotated constructor found for class " + targetClass.getName(), exception);
      }
    }
  }
}
