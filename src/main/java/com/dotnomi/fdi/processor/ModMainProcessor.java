package com.dotnomi.fdi.processor;

import com.dotnomi.fdi.annotation.ModMain;
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
import java.util.HashSet;
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
  private static final Set<String> foundModMainClasses = new HashSet<>();
  private static boolean errorReportedInThisCompilation = false;

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver() || errorReportedInThisCompilation) {
      return false;
    }

    Set<? extends Element> currentRoundAnnotatedElements = roundEnv.getElementsAnnotatedWith(ModMain.class);
    Messager messager = this.processingEnv.getMessager();

    for (Element annotatedElement : currentRoundAnnotatedElements) {
      String className = ((TypeElement) annotatedElement).getQualifiedName().toString();
      if (foundModMainClasses.contains(className)) {
        continue;
      }

      foundModMainClasses.add(className);

      if (foundModMainClasses.size() > 1) {
        if (!errorReportedInThisCompilation) {
          messager.printMessage(
            Diagnostic.Kind.ERROR,
            "Multiple @ModMain annotated classes found. Only one is permitted in the entire project."
          );
          errorReportedInThisCompilation = true;
        }

        messager.printMessage(
          Diagnostic.Kind.ERROR,
          "Found @ModMain annotated class: " + annotatedElement
        );
      }
    }

    if (roundEnv.processingOver()) {
      foundModMainClasses.clear();
      errorReportedInThisCompilation = false;
    }

    return false;
  }
}
