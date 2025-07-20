package com.dotnomi.fdi.annotation;

import com.dotnomi.fdi.ModInjector;

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
  String packageName() default "";
}
