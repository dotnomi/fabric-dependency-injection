package com.dotnomi.fabricdependencyinjection.annotation;

import com.dotnomi.fabricdependencyinjection.ModInjector;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as the main entry point for the mod, triggering the
 * initialization of the {@link ModInjector}.
 * <p>
 * There must be exactly one class annotated with {@code @ModMain} in the mod.
 * This class itself is also considered {@link ModScoped} and is managed by the container.
 *
 * @see ModInjector
 * @see ModScoped
 */
@ModScoped
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ModMain {
  /**
   * Specifies the root package name to be scanned for manageable components.
   * <p>
   * If left empty (the default), the framework will use the package of the class
   * annotated with {@code @ModMain} as the base for scanning.
   *
   * @return The custom base package name for the classpath scan.
   */
  @NotNull String packageName() default "";
}
