package com.dotnomi.fabricdependencyinjection.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed after dependency injection is done to perform any initialization.
 * The annotated method must not have any parameters.
 * It will be invoked after the constructor has been called and all dependencies have been injected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PostConstruct {
}
