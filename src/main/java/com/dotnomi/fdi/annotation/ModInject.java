package com.dotnomi.fdi.annotation;

import com.dotnomi.fdi.ModInjector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor or a field to be a target for dependency injection.
 * <ul>
 * <li><b>On a constructor:</b> Signals the {@link ModInjector} to use this
 * constructor to create an instance of the class. All parameters of this
 * constructor will be resolved as dependencies. A class should have at
 * most one constructor marked with this annotation.</li>
 * <li><b>On a field:</b> Signals the {@link ModInjector} to automatically
 * inject a managed instance into this field after the object has been
 * instantiated.</li>
 * </ul>
 *
 * @see ModInjector
 * @see ModScoped
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface ModInject {
}
