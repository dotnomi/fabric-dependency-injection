package com.dotnomi.fabricdependencyinjection;

import com.dotnomi.fabricdependencyinjection.annotation.ModIdentifier;
import com.dotnomi.fabricdependencyinjection.annotation.ModInject;
import com.dotnomi.fabricdependencyinjection.annotation.ModMain;
import com.dotnomi.fabricdependencyinjection.annotation.ModScoped;
import com.dotnomi.fabricdependencyinjection.annotation.PostConstruct;
import com.dotnomi.fabricdependencyinjection.exception.CircularDependencyException;
import com.dotnomi.fabricdependencyinjection.exception.ContainerAlreadyInitializedException;
import com.dotnomi.fabricdependencyinjection.exception.ContainerNotInitializedException;
import com.dotnomi.fabricdependencyinjection.exception.InstanceCreationException;
import com.dotnomi.fabricdependencyinjection.exception.ModInjectorException;
import com.dotnomi.fabricdependencyinjection.exception.MultipleInjectableConstructorsException;
import com.dotnomi.fabricdependencyinjection.exception.MultiplePostConstructMethodsException;
import com.dotnomi.fabricdependencyinjection.exception.NoInjectableConstructorException;
import com.dotnomi.fabricdependencyinjection.exception.NoMainClassException;
import com.dotnomi.fabricdependencyinjection.exception.PostConstructMethodHasParametersException;
import com.dotnomi.fabricdependencyinjection.exception.TooManyInstancesFoundException;
import com.dotnomi.fabricdependencyinjection.exception.UnmanagedClassException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The internal, stateful container that manages the DI lifecycle for a single mod.
 * This class handles classpath scanning, instance creation, dependency resolution, and caching.
 * It is designed to be thread-safe. This class is not intended for direct use by end-users.
 */
final class ModContainer {
  private static final Logger logger = LoggerFactory.getLogger(ModContainer.class);

  /**
   * Registry for storing and retrieving created singleton instances.
   */
  private final BeanRegistry beanRegistry = new BeanRegistry();

  /**
   * A queue to manage the order of class instantiation during initialization.
   * Classes with list dependencies are processed last.
   */
  private final Queue<Class<?>> instanceCreationQueue = new ConcurrentLinkedQueue<>();

  /**
   * Stores the current instantiation path for the calling thread.
   * This is used to detect circular dependencies. A List is used to preserve the order of instantiation.
   * Using ThreadLocal ensures that dependency tracking is isolated per thread.
   */
  private final ThreadLocal<List<Class<?>>> dependencyStack = ThreadLocal.withInitial(ArrayList::new);

  /**
   * The unique identifier for the mod this container belongs to.
   */
  private final String modId;

  /**
   * The current status of the container. Volatile to ensure visibility across threads.
   */
  private volatile ContainerStatus status = ContainerStatus.INITIALIZING;

  /**
   * A cache of all classes annotated with {@link ModScoped}.
   * This list is populated during the initial classpath scan and is used to resolve dependencies.
   */
  private final List<Class<?>> managedClasses = new ArrayList<>();

  /**
   * Constructs a new container for the specified mod ID.
   *
   * @param modId The unique ID of the mod.
   */
  public ModContainer(@NotNull String modId) {
    this.modId = modId;
  }

  /**
   * Gets the current operational status of the container.
   *
   * @return The current {@link ContainerStatus}.
   */
  public @NotNull ContainerStatus getStatus() {
    return status;
  }

  /**
   * Initializes the container. This method performs classpath scanning, identifies all {@link ModScoped} classes,
   * and pre-instantiates them to resolve all dependencies eagerly.
   * This method is synchronized to prevent concurrent initialization.
   *
   * @param mainInstance The main class of the mod, annotated with {@link ModMain}, used to determine the scanning root package.
   * @throws ContainerAlreadyInitializedException If the container has already been initialized.
   * @throws NoMainClassException If the provided class is not annotated with {@link ModMain}.
   * @throws ModInjectorException If any error occurs during the initialization process.
   */
  public synchronized void initialize(@NotNull Object mainInstance) {
    if (this.status != ContainerStatus.INITIALIZING) {
      throw new ContainerAlreadyInitializedException(String.format("The container for mod '%s' has already been initialized. The initialize() method must only be called once.", modId));
    }

    logger.debug("[{}] Starting container initialization...", modId);

    var mainClass = mainInstance.getClass();
    if (!mainClass.isAnnotationPresent(ModMain.class)) {
      throw new NoMainClassException(String.format("The provided main class '%s' is not annotated with @ModMain. Please add the annotation to your mod's main class to allow it to be discovered.", mainClass.getName()));
    }

    beanRegistry.registerInstance(mainClass, mainInstance, null);

    var modMainAnnotation = mainClass.getAnnotation(ModMain.class);
    var basePackage = modMainAnnotation.packageName().isEmpty()
      ? mainClass.getPackage().getName()
      : modMainAnnotation.packageName();

    logger.info("[{}] Starting classpath scanning for package: {}", modId, basePackage);
    var reflections = new Reflections(basePackage);
    managedClasses.addAll(reflections.getTypesAnnotatedWith(ModScoped.class));
    instanceCreationQueue.addAll(managedClasses);
    logger.debug("[{}] Found {} @ModScoped classes to manage: {}", modId, instanceCreationQueue.size(), instanceCreationQueue);

    try {
      while (!instanceCreationQueue.isEmpty()) {
        var modScopedClass = instanceCreationQueue.poll();
        try {
          getInstanceOf(modScopedClass, null);
        } catch (Exception exception) {
          instanceCreationQueue.clear();
          logger.error("[{}] Failed to pre-instantiate or register class {} for DI.", modId, modScopedClass.getName(), exception);
          throw exception;
        }
      }

      injectFields(mainInstance);
      invokePostConstructMethod(mainInstance);
      this.status = ContainerStatus.RUNNING;
      logger.info("[{}] ModInjector initialized successfully.", modId);
    } catch (IllegalAccessException exception) {
      this.status = ContainerStatus.FAILED;
      throw new ModInjectorException(String.format("Failed to inject fields for main class '%s'. Ensure that all fields annotated with @ModInject are accessible (e.g., not 'final').", mainClass.getName()), exception);
    } catch (Exception exception) {
      this.status = ContainerStatus.FAILED;
      throw exception;
    }
  }

  /**
   * Retrieves a managed instance of the specified class. If an instance doesn't exist,
   * it is created, registered, and returned. This method uses a double-checked locking
   * pattern to ensure thread-safe lazy instantiation.
   *
   * @param targetClass The class type of the instance to retrieve.
   * @param identifier The optional identifier to distinguish between multiple instances of the same type.
   * @param <T> The type of the class.
   * @return The singleton instance of the requested class.
   * @throws ContainerNotInitializedException If the container is in a FAILED state.
   * @throws UnmanagedClassException If the injector does not manage the requested class.
   * @throws ModInjectorException If any error occurs during instance creation.
   */
  public <T> @NotNull T getInstanceOf(@NotNull Class<T> targetClass, @Nullable String identifier) {
    if (this.status == ContainerStatus.FAILED) {
      throw new ContainerNotInitializedException(String.format("The container for mod '%s' is in a FAILED state and cannot be used. Please check the logs for an earlier error that occurred during initialization.", modId));
    }

    logger.debug("[{}] Request for instance of type [{}] with identifier [{}].", modId, targetClass.getName(), identifier);

    var instance = beanRegistry.findInstance(targetClass, identifier);
    if (instance == null) {
      // Double-checked locking for thread-safe lazy instantiation.
      synchronized (this) {
        instance = beanRegistry.findInstance(targetClass, identifier);
        if (instance == null) {
          logger.debug("[{}] Cache miss for [{}]. Proceeding to create new instance.", modId, targetClass.getName());

          var isAbstractType = Modifier.isAbstract(targetClass.getModifiers());
          if (isAbstractType) {
            var concreteClass = findConcreteClassFor(targetClass, identifier);
            return targetClass.cast(getInstanceOf(concreteClass, identifier));
          }

          if (targetClass.isAnnotationPresent(ModScoped.class) || targetClass.isAnnotationPresent(ModMain.class)) {
            return targetClass.cast(createInstance(targetClass));
          }

          throw new UnmanagedClassException(String.format("The class '%s' cannot be provided because it is not managed by the container. To fix this, annotate the class with @ModScoped.", targetClass.getName()));
        } else {
          logger.debug("[{}] Cache hit for [{}] after acquiring lock.", modId, targetClass.getName());
        }
      }
    } else {
      logger.debug("[{}] Cache hit for [{}]. Returning existing instance.", modId, targetClass.getName());
    }

    return targetClass.cast(instance);
  }

  private <T> @NotNull Class<?> findConcreteClassFor(@NotNull Class<T> targetClass, @Nullable String identifier) {
    if (!Modifier.isAbstract(targetClass.getModifiers()) && targetClass.isAnnotationPresent(ModScoped.class)) {
      return targetClass;
    }

    var implementations = managedClasses.stream()
      .filter(targetClass::isAssignableFrom)
      .filter(clazz -> !clazz.isInterface())
      .filter(clazz -> {
        if (identifier == null) return true;
        return clazz.isAnnotationPresent(ModIdentifier.class) && identifier.equals(clazz.getAnnotation(ModIdentifier.class).value());
      }).toList();

    if (implementations.isEmpty()) {
      throw new InstanceCreationException(String.format("Dependency resolution failed: Could not find a suitable implementation for '%s'. Ensure a class implements this interface, is annotated with @ModScoped, and has the correct @ModIdentifier ('%s') if used.", targetClass.getName(), identifier));
    }

    if (implementations.size() > 1) {
      throw new TooManyInstancesFoundException(String.format("Ambiguous dependency: Found multiple implementations for '%s'. Use @ModIdentifier on the injection point or the class to specify which one to use.", targetClass.getName()));
    }

    return implementations.getFirst();
  }

  /**
   * Retrieves all managed instances that are assignable to the specified class or interface.
   *
   * @param targetClass The class or interface type to match against.
   * @param <T> The type of the class.
   * @return A {@link BeanList} containing all matching managed instances.
   * @throws ContainerNotInitializedException If the container is in a FAILED state.
   */
  public <T> @NotNull BeanList<T> getInstancesOf(@NotNull Class<T> targetClass) {
    if (this.status == ContainerStatus.FAILED) {
      throw new ContainerNotInitializedException(String.format("The container for mod '%s' is in a FAILED state and cannot be used. Please check the logs for an earlier error that occurred during initialization.", modId));
    }

    logger.debug("[{}] Request for all instances of type [{}].", modId, targetClass.getName());

    var candidates = managedClasses.stream()
      .filter(managedClass -> !managedClass.isInterface() && !Modifier.isAbstract(managedClass.getModifiers()))
      .filter(targetClass::isAssignableFrom)
      .toList();

    var instances = candidates.stream()
      .map(candidate -> getInstanceOf(candidate, null))
      .map(targetClass::cast).toList();

    if (instances.isEmpty()) {
      logger.warn("[{}] No instances found for type [{}]. Returning empty list.", modId, targetClass.getName());
    }

    return new BeanList<>(instances);
  }

  /**
   * Creates a new instance of the given class, resolving and injecting all its dependencies.
   * This method handles constructor injection, field injection, and PostConstruct method invocation.
   * It also tracks the dependency path to detect circular dependencies.
   *
   * @param targetClass The class to instantiate.
   * @param <T> The type of the class.
   * @return The newly created and fully initialized instance.
   * @throws CircularDependencyException If a circular dependency is detected.
   * @throws InstanceCreationException If instantiation fails for any reason.
   */
  private <T> @NotNull T createInstance(@NotNull Class<T> targetClass) {
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

      var args = resolveConstructorArguments(injectableConstructor);
      var newInstance = targetClass.cast(injectableConstructor.newInstance(args));
      logger.debug("[{}] Successfully instantiated [{}]. Proceeding with field injection.", modId, targetClass.getName());

      logger.debug("[{}] Registering new instance of [{}] under its concrete class.", modId, newInstance.getClass().getName());
      beanRegistry.registerInstance(newInstance.getClass(), newInstance, null);

      injectFields(newInstance);
      invokePostConstructMethod(newInstance);

      return newInstance;
    } catch (Exception exception) {
      if (exception instanceof ModInjectorException modInjectorException) {
        throw modInjectorException;
      }

      logger.error("[{}] Failed to create an instance of class [{}].", modId, targetClass.getName(), exception);
      throw new InstanceCreationException(String.format("An unexpected error occurred while creating an instance of '%s'. Check the class's constructor and initialization logic for errors.", targetClass.getName()), exception);
    } finally {
      // Clean up the dependency stack for the current thread.
      if (!dependencyStack.get().isEmpty()) {
        dependencyStack.get().removeLast();
      }
    }
  }

  /**
   * Resolves the arguments for a given constructor by retrieving required dependencies from the container.
   *
   * @param constructor The constructor for which to resolve arguments.
   * @return An array of objects representing the resolved constructor arguments.
   */
  private @NotNull Object[] resolveConstructorArguments(@NotNull Constructor<?> constructor) {
    var parameters = constructor.getParameters();
    var args = new Object[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      var parameter = parameters[i];
      var paramType = parameter.getType();
      // Handle injection of a list of beans.
      if (BeanList.class.isAssignableFrom(paramType) && parameter.getParameterizedType() instanceof ParameterizedType parameterizedType) {
        var listType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        logger.debug("[{}] Resolving list dependency of type List<{}> for constructor.", modId, listType.getName());
        args[i] = getInstancesOf(listType);
      } else {
        // Handle injection of a single bean.
        var identifier = parameter.isAnnotationPresent(ModIdentifier.class)
          ? parameter.getAnnotation(ModIdentifier.class).value()
          : null;
        logger.debug("[{}] Resolving constructor dependency of type [{}] with identifier [{}] for [{}].", modId, paramType.getName(), identifier, constructor.getDeclaringClass().getName());
        args[i] = getInstanceOf(paramType, identifier);
      }
    }
    return args;
  }

  /**
   * Performs field injection on a given instance. It scans for fields annotated with {@link ModInject}
   * and injects the corresponding dependencies.
   *
   * @param instance The object instance to inject fields into.
   * @throws IllegalAccessException If a field cannot be accessed.
   */
  private void injectFields(@NotNull Object instance) throws IllegalAccessException {
    var currentClass = instance.getClass();
    while (currentClass != null && currentClass != Object.class) {
      for (var field : currentClass.getDeclaredFields()) {
        if (field.isAnnotationPresent(ModInject.class)) {
          field.setAccessible(true);
          var fieldType = field.getType();
          // Handle injection of a list of beans.
          if (BeanList.class.isAssignableFrom(fieldType)) {
            var parameterizedType = (ParameterizedType) field.getGenericType();
            var listType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            logger.debug("[{}] Found @ModInject on list field [{}] in class [{}].", modId, field.getName(), instance.getClass().getName());
            var dependencies = getInstancesOf(listType);
            field.set(instance, dependencies);
            logger.debug("[{}] Injected {} dependencies of type [{}] into field [{}].", modId, dependencies.size(), listType.getName(), field.getName());
          } else {
            // Handle injection of a single bean.
            var identifier = field.isAnnotationPresent(ModIdentifier.class) ? field.getAnnotation(ModIdentifier.class).value() : null;
            logger.debug("[{}] Found @ModInject on field [{}] in class [{}].", modId, field.getName(), instance.getClass().getName());
            var dependency = getInstanceOf(fieldType, identifier);
            field.set(instance, dependency);
            logger.debug("[{}] Injected dependency of type [{}] into field [{}].", modId, dependency.getClass().getName(), field.getName());
          }
        }
      }
      currentClass = currentClass.getSuperclass(); // Traverse up the class hierarchy.
    }
  }

  /**
   * Invokes the method annotated with {@link PostConstruct} on the given instance.
   * This method is called after all dependencies have been injected.
   *
   * @param instance The object instance on which to invoke the PostConstruct method.
   * @throws InstanceCreationException If the PostConstruct method cannot be invoked.
   */
  private void invokePostConstructMethod(@NotNull Object instance) {
    var postConstructMethods = new ArrayList<Method>();
    var currentClass = instance.getClass();

    while (currentClass != null && currentClass != Object.class) {
      for (var method : currentClass.getDeclaredMethods()) {
        if (method.isAnnotationPresent(PostConstruct.class)) {
          postConstructMethods.add(method);
        }
      }
      currentClass = currentClass.getSuperclass(); // Traverse up the class hierarchy.
    }

    if (postConstructMethods.size() > 1) {
      throw new MultiplePostConstructMethodsException(String.format("Found multiple methods annotated with @PostConstruct in the class hierarchy of '%s'. Only one @PostConstruct method is allowed.", instance.getClass().getName()));
    }

    if (!postConstructMethods.isEmpty()) {
      var method = postConstructMethods.getFirst();
      logger.debug("[{}] Found @PostConstruct on method [{}] in class [{}].", modId, method.getName(), instance.getClass().getName());

      if (method.getParameterCount() != 0) {
        throw new PostConstructMethodHasParametersException(String.format("The @PostConstruct method '%s' must not have any parameters. Please remove the parameters from the method signature.", method.getName()));
      }

      try {
        method.setAccessible(true);
        method.invoke(instance);
      } catch (Exception exception) {
        throw new InstanceCreationException(String.format("Failed to invoke the @PostConstruct method '%s'. Please check the code inside this method for errors.", method.getName()), exception);
      }
    }
  }

  /**
   * Finds the appropriate constructor for instantiating a class.
   * It prioritizes a constructor annotated with {@link ModInject}. If none is found,
   * it falls back to the public no-argument constructor.
   *
   * @param targetClass The class to find the constructor for.
   * @param <T> The type of the class.
   * @return The injectable {@link Constructor}.
   * @throws MultipleInjectableConstructorsException If more than one constructor is annotated with {@link ModInject}.
   * @throws NoInjectableConstructorException If no suitable constructor is found.
   */
  private <T> @NotNull Constructor<?> findInjectableConstructor(@NotNull Class<T> targetClass) {
    var injectableConstructors = Arrays.stream(targetClass.getConstructors())
      .filter(constructor -> constructor.isAnnotationPresent(ModInject.class))
      .toList();

    if (injectableConstructors.size() > 1) {
      throw new MultipleInjectableConstructorsException(String.format("Class '%s' has multiple constructors annotated with @ModInject. Only one constructor can be marked for injection.", targetClass.getName()));
    }

    if (!injectableConstructors.isEmpty()) {
      return injectableConstructors.getFirst();
    }

    try {
      return targetClass.getConstructor();
    } catch (NoSuchMethodException exception) {
      throw new NoInjectableConstructorException(String.format("Could not find a suitable constructor for class '%s'. To make it injectable, provide a public no-argument constructor or annotate exactly one constructor with @ModInject.", targetClass.getName()), exception);
    }
  }
}
