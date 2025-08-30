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
import com.dotnomi.fabricdependencyinjection.exception.TooManyInstancesFoundException;
import com.dotnomi.fabricdependencyinjection.exception.UnmanagedClassException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The public-facing static API for the dependency injection framework.
 * This class provides methods to initialize the DI container for a mod and to retrieve managed instances.
 * It manages a map of {@link ModContainer} instances, one for each mod ID.
 * This class is thread-safe.
 */
public final class ModInjector {
  /**
   * A thread-safe map that holds a dedicated {@link ModContainer} for each mod ID.
   */
  private static final Map<String, ModContainer> modContainers = new ConcurrentHashMap<>();

  /**
   * Initializes the dependency injection container for a specific mod.
   * This method scans the classpath for manageable classes and pre-instantiates them.
   * It must be called once at the beginning of the mod's lifecycle.
   *
   * @param modId The unique ID of the mod.
   * @param mainInstance The main class of the mod, annotated with {@link ModMain}.
   * This class defines the base package for classpath scanning.
   * @throws ContainerAlreadyInitializedException if the container for the given modId has already been initialized.
   * @throws NoMainClassException if the main class is not annotated with {@link ModMain}.
   * @throws UnmanagedClassException if a requested class is not managed by the injector
   * (i.e., not annotated with {@link ModScoped}).
   * @throws CircularDependencyException if a circular dependency is detected during instantiation.
   * @throws MultipleInjectableConstructorsException if a class has multiple constructors annotated with {@link ModInject}.
   * @throws NoInjectableConstructorException if no suitable constructor is found for a class.
   * @throws InstanceCreationException if an unexpected error occurs during instantiation.
   * @throws ModInjectorException for any other framework-specific errors.
   */
  public static void initialize(@NotNull String modId, @NotNull Object mainInstance) throws ModInjectorException {
    modContainers.computeIfAbsent(modId, ModContainer::new).initialize(mainInstance);
  }

  /**
   * Retrieves a managed instance of the specified class from the container.
   *
   * @param modId The unique ID of the mod whose container should be used.
   * @param targetClass The class type of the instance to retrieve.
   * @param <T> The type of the class.
   * @return The singleton instance of the requested class.
   * @throws ContainerNotInitializedException if the container has not been initialized yet.
   * @throws UnmanagedClassException if the injector does not manage the requested class.
   * @throws TooManyInstancesFoundException if more than one matching instance is found without an identifier.
   * @throws CircularDependencyException if a circular dependency is detected during lazy instantiation.
   * @throws ModInjectorException for any other framework-specific errors.
   */
  public static <T> @NotNull T getInstanceOf(@NotNull String modId, @NotNull Class<T> targetClass) throws ModInjectorException {
    return getInstanceOf(modId, targetClass, null);
  }

  /**
   * Retrieves a managed instance of the specified class from the container, filtered by an identifier.
   *
   * @param modId The unique ID of the mod whose container should be used.
   * @param targetClass The class type of the instance to retrieve.
   * @param identifier The unique identifier for the instance.
   * @param <T> The type of the class.
   * @return The singleton instance of the requested class.
   * @throws ContainerNotInitializedException if the container has not been initialized yet.
   * @throws UnmanagedClassException if the injector does not manage the requested class.
   * @throws TooManyInstancesFoundException if more than one matching instance is found.
   * @throws CircularDependencyException if a circular dependency is detected during lazy instantiation.
   * @throws ModInjectorException for any other framework-specific errors.
   */
  public static <T> @NotNull T getInstanceOf(@NotNull String modId, @NotNull Class<T> targetClass, @Nullable String identifier) throws ModInjectorException {
    var modContainer = getModContainer(modId);
    return modContainer.getInstanceOf(targetClass, identifier);
  }

  /**
   * Retrieves all managed instances that match the specified class or interface from the container.
   *
   * @param modId The unique ID of the mod whose container should be used.
   * @param targetClass The class type or interface type to match instances against.
   * @param <T> The type of the class.
   * @return A {@link BeanList} of all matching managed instances.
   * @throws ContainerNotInitializedException if the container has not been initialized yet.
   * @throws ModInjectorException for any other framework-specific errors.
   */
  public static <T> @NotNull BeanList<T> getInstancesOf(@NotNull String modId, @NotNull Class<T> targetClass) throws ModInjectorException {
    var modContainer = getModContainer(modId);
    return modContainer.getInstancesOf(targetClass);
  }

  /**
   * A private helper method to retrieve the correct {@link ModContainer} for a given mod ID.
   * It performs checks to ensure the container exists and is in a usable state.
   *
   * @param modId The unique ID of the mod.
   * @return The corresponding {@link ModContainer}.
   * @throws ContainerNotInitializedException if the container does not exist or is still initializing.
   */
  private static @NotNull ModContainer getModContainer(@NotNull String modId) {
    var modContainer = modContainers.get(modId);
    if (modContainer == null) {
      throw new ContainerNotInitializedException("ModContainer has not been initialized yet. Call ModInjector.initialize() first.");
    } else if (modContainer.getStatus() == ContainerStatus.INITIALIZING) {
      throw new ContainerNotInitializedException("ModContainer for modId '" + modId + "' is still initializing.");
    }
    return modContainer;
  }
}
