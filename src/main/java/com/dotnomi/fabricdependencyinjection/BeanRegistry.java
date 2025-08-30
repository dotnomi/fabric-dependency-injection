package com.dotnomi.fabricdependencyinjection;

import com.dotnomi.fabricdependencyinjection.annotation.ModIdentifier;
import com.dotnomi.fabricdependencyinjection.exception.TooManyInstancesFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A private, thread-safe utility class to manage the registry of singleton instances.
 * It handles storing and retrieving instances by their class and an optional identifier.
 * The internal structure is a map of maps: {@code Map<Class<?>, Map<String, Object>>}.
 */
final class BeanRegistry {
  /**
   * The primary data structure for storing instances.
   * - The outer map's key is the concrete class of the instance.
   * - The inner map's key is the identifier (an empty string for non-identified instances),
   * and the value is the singleton instance itself.
   */
  private final Map<Class<?>, Map<String, Object>> instances = new ConcurrentHashMap<>();

  /**
   * Registers a newly created instance in the registry.
   * The identifier is determined first by the provided parameter, then by the {@link ModIdentifier}
   * annotation on the class, and finally defaults to an empty string.
   *
   * @param targetClass The concrete class of the instance.
   * @param instance The instance object to register.
   * @param identifier The optional identifier. Can be null.
   */
  public void registerInstance(@NotNull Class<?> targetClass, @NotNull Object instance, @Nullable String identifier) {
    var instanceIdentifier = Optional.ofNullable(identifier).orElse(getInstanceIdentifier(instance).orElse(""));
    instances.computeIfAbsent(targetClass, key -> new ConcurrentHashMap<>())
      .putIfAbsent(instanceIdentifier, instance);
  }

  /**
   * Finds a single instance matching the given class and optional identifier.
   *
   * @param targetClass The class type to find.
   * @param identifier The optional identifier. If null, a unique instance of the type is sought.
   * @return The found instance, or null if no matching instance exists.
   * @throws TooManyInstancesFoundException if the identifier is null and multiple instances of the
   *         assignable type are found.
   */
  public @Nullable Object findInstance(@NotNull Class<?> targetClass, @Nullable String identifier) {
    // Case 1: An identifier is provided.
    if (identifier != null) {
      // If the target is a concrete class, we can look it up directly (fast path).
      if (!Modifier.isAbstract(targetClass.getModifiers())) {
        var map = instances.get(targetClass);
        return (map != null) ? map.get(identifier) : null;
      }

      // If the target is an interface or an abstract class, we must scan all instances.
      List<Object> matchingInstances = instances.values().stream()
        .flatMap(map -> map.values().stream())
        .filter(instance -> targetClass.isAssignableFrom(instance.getClass()))
        .filter(instance -> getInstanceIdentifier(instance).map(id -> id.equals(identifier)).orElse(false))
        .toList();

      if (matchingInstances.size() > 1) {
        throw new TooManyInstancesFoundException("Found multiple instances of type " + targetClass.getName() + " with identifier '" + identifier + "'");
      }

      return matchingInstances.isEmpty() ? null : matchingInstances.getFirst();
    }

    // Case 2: No identifier is provided.
    var matchingInstances = findInstances(targetClass);
    if (matchingInstances.size() > 1) {
      throw new TooManyInstancesFoundException("Multiple instances found for class/interface " + targetClass.getName() + " and no unique identifier was provided.");
    }

    return matchingInstances.isEmpty() ? null : matchingInstances.getFirst();
  }

  /**
   * Finds all instances that are assignable to the given class or interface.
   *
   * @param targetClass The class or interface type to match against.
   * @return A list of all matching instances. The list is immutable.
   */
  public @NotNull List<Object> findInstances(@NotNull Class<?> targetClass) {
    // If the target is an interface or an abstract class, we must check all registered instances.
    if (Modifier.isAbstract(targetClass.getModifiers())) {
      return instances.values().stream()
        .flatMap(map -> map.values().stream())
        .filter(instance -> targetClass.isAssignableFrom(instance.getClass()))
        .toList();
    }

    // If the target is a class, we can optimize by checking only assignable class keys.
    return instances.entrySet().stream()
      .filter(entry -> targetClass.isAssignableFrom(entry.getKey()))
      .flatMap(entry -> entry.getValue().values().stream())
      .toList();
  }

  /**
   * Helper method to get the identifier from a class annotation.
   */
  private @NotNull Optional<String> getInstanceIdentifier(@NotNull Object instance) {
    Class<?> instanceClass = instance.getClass();
    if (instanceClass.isAnnotationPresent(ModIdentifier.class)) {
      return Optional.of(instanceClass.getAnnotation(ModIdentifier.class).value());
    }
    return Optional.empty();
  }
}
