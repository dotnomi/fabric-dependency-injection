package com.dotnomi.ddi.processor;

import com.dotnomi.ddi.annotation.ModMain;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * An annotation processor that enforces that exactly one {@link ModMain} annotation
 * exists in the compiled sources.
 * If more than one is found, the build will fail with an error.
 */
@SupportedAnnotationTypes("com.dotnomi.ddi.annotation.ModMain")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public final class ModMainProcessor extends AbstractProcessor {
  private boolean hasRun = false;

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (hasRun) {
      return true;
    }

    Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(ModMain.class);

    if (annotatedElements.size() > 1) {
      Messager messager = this.processingEnv.getMessager();

      messager.printMessage(
        Diagnostic.Kind.ERROR,
        "Multiple @ModMain annotated classes found. Only one is permitted."
      );

      for (Element annotatedElement : annotatedElements) {
        messager.printMessage(
          Diagnostic.Kind.ERROR,
          "Found @ModMain annotated class: " + annotatedElement.toString()
        );
      }
    }

    hasRun = true;
    return true;
  }
}
