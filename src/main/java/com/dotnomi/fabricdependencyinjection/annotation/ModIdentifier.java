package com.dotnomi.fabricdependencyinjection.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a unique identifier for a managed bean.
 * This annotation can be used on a class, a constructor parameter, or a field
 * to distinguish between multiple beans of the same type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
public @interface ModIdentifier {
  /**
   * The unique identifier string.
   * @return The identifier value.
   */
  @NotNull String value();
}
