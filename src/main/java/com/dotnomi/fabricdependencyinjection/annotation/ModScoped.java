package com.dotnomi.fabricdependencyinjection.annotation;

import com.dotnomi.fabricdependencyinjection.ModInjector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a component to be managed by the {@link ModInjector}.
 * <p>
 * Classes annotated with {@code @ModScoped} are discovered during the classpath
 * scan at initialization. The injector creates and manages a single instance (singleton scope)
 * of each such class, which can then be injected into other components.
 *
 * @see ModInjector
 * @see ModInject
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ModScoped {
}
