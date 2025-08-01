package com.dotnomi.fabricdependencyinjection;

import com.dotnomi.fabricdependencyinjection.exception.CircularDependencyException;
import com.dotnomi.fabricdependencyinjection.exception.ContainerAlreadyInitializedException;
import com.dotnomi.fabricdependencyinjection.exception.ContainerNotInitializedException;
import com.dotnomi.fabricdependencyinjection.exception.InstanceCreationException;
import com.dotnomi.fabricdependencyinjection.exception.MultipleInjectableConstructorsException;
import com.dotnomi.fabricdependencyinjection.exception.NoInjectableConstructorException;
import com.dotnomi.fabricdependencyinjection.exception.NoMainClassException;
import com.dotnomi.fabricdependencyinjection.exception.UnmanagedClassException;
import com.dotnomi.fabricdependencyinjection.testclasses.failingconstructor.FailingConstructorTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.inheritance.BaseService;
import com.dotnomi.fabricdependencyinjection.testclasses.circular.CircularTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.inheritance.SubServiceTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.multipleconstructors.MultipleConstructorsTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.noannotation.NoAnnotationTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.noconstructor.NoConstructorTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.nopackagevariable.NoPackageVariableTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.nopackagevariable.ServiceInSamePackage;
import com.dotnomi.fabricdependencyinjection.testclasses.postconstruct.failing.FailingPostConstructTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.postconstruct.multiple.MultiplePostConstructService;
import com.dotnomi.fabricdependencyinjection.testclasses.postconstruct.multiple.MultiplePostConstructTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.postconstruct.parameter.ParameterPostConstructTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.postconstruct.success.PostConstructService;
import com.dotnomi.fabricdependencyinjection.testclasses.postconstruct.success.PostConstructTestModMain;
import com.dotnomi.fabricdependencyinjection.testclasses.unmanaged.UnmanagedClass;
import com.dotnomi.fabricdependencyinjection.testclasses.success.ServiceA;
import com.dotnomi.fabricdependencyinjection.testclasses.inheritance.SubService;
import com.dotnomi.fabricdependencyinjection.testclasses.success.SuccessModMain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModInjectorTest {
  private static final String MOD_ID = "test-mod";

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() throws Exception {
    var modContainersField = ModInjector.class.getDeclaredField("modContainers");
    modContainersField.setAccessible(true);
    var modContainers = (Map<String, ?>) modContainersField.get(null);
    modContainers.clear();
  }

  @Test
  @DisplayName("@PostConstruct method is called successfully after instantiation")
  void postConstruct_methodIsCalledSuccessfully() {
    assertDoesNotThrow(() -> ModInjector.initialize(MOD_ID, PostConstructTestModMain.class));

    var service = ModInjector.getInstanceOf(MOD_ID, PostConstructService.class);

    assertNotNull(service);
    assertTrue(service.isInitialized(), "@PostConstruct method should have been called.");
  }

  @Test
  @DisplayName("Executes only the first found @PostConstruct method without error")
  void postConstruct_handlesMultipleAnnotationsGracefully() {
    assertDoesNotThrow(() -> ModInjector.initialize(MOD_ID, MultiplePostConstructTestModMain.class));

    var service = ModInjector.getInstanceOf(MOD_ID, MultiplePostConstructService.class);

    assertNotNull(service);
    assertTrue(service.isFirstBoolean(), "First @PostConstruct method should have been called.");
    assertFalse(service.isSecondBoolean(), "Second @PostConstruct method should have not been called.");
  }

  @Test
  @DisplayName("Successful initialization and instance retrieval")
  void initialize_andGetInstance_successfully() {
    assertDoesNotThrow(() -> ModInjector.initialize(MOD_ID, SuccessModMain.class));
    var mainInstance = ModInjector.getInstanceOf(MOD_ID, ServiceA.class);
    assertNotNull(mainInstance);
    assertNotNull(mainInstance.getServiceB());
  }

  @Test
  @DisplayName("Succeeds when packageName in @ModMain is omitted")
  void initialize_succeedsWhenPackageNameIsOmitted() {
    assertDoesNotThrow(() -> ModInjector.initialize(MOD_ID, NoPackageVariableTestModMain.class));
    assertNotNull(ModInjector.getInstanceOf(MOD_ID, ServiceInSamePackage.class));
  }

  @Test
  @DisplayName("Returns same instance on multiple calls (Caching)")
  void getInstanceOf_returnsSameInstance() {
    ModInjector.initialize(MOD_ID, SuccessModMain.class);
    var instance1 = ModInjector.getInstanceOf(MOD_ID, ServiceA.class);
    var instance2 = ModInjector.getInstanceOf(MOD_ID, ServiceA.class);
    assertSame(instance1, instance2, "Should return the same singleton instance.");
  }

  @Test
  @DisplayName("Throws InstanceCreationException if @PostConstruct method has parameters")
  void postConstruct_throwsIfMethodHasParameters() {
    assertThrows(InstanceCreationException.class, () ->
      ModInjector.initialize(MOD_ID, ParameterPostConstructTestModMain.class)
    );
  }

  @Test
  @DisplayName("Throws InstanceCreationException if @PostConstruct method fails")
  void postConstruct_throwsIfMethodFails() {
    assertThrows(InstanceCreationException.class, () ->
      ModInjector.initialize(MOD_ID, FailingPostConstructTestModMain.class)
    );
  }

  @Test
  @DisplayName("Throws ContainerNotInitializedException if getInstanceOf is called before initialize")
  void getInstanceOf_throwsWhenContainerIsNotInitialized() {
    assertThrows(ContainerNotInitializedException.class, () ->
      ModInjector.getInstanceOf(MOD_ID, ServiceA.class)
    );
  }

  @Test
  @DisplayName("Throws ContainerAlreadyInitializedException on second initialize call")
  void initialize_throwsIfAlreadyInitialized() {
    ModInjector.initialize(MOD_ID, SuccessModMain.class);
    assertThrows(ContainerAlreadyInitializedException.class, () ->
      ModInjector.initialize(MOD_ID, SuccessModMain.class)
    );
  }

  @Test
  @DisplayName("Throws ContainerNotInitializedException if not initialized")
  void getInstanceOf_throwsIfNotInitialized() {
    assertThrows(ContainerNotInitializedException.class, () ->
      ModInjector.getInstanceOf(MOD_ID, ServiceA.class)
    );
  }

  @Test
  @DisplayName("Throws UnmanagedClassException for class without @ModScoped")
  void getInstanceOf_throwsIfClassNotManaged() {
    ModInjector.initialize(MOD_ID, SuccessModMain.class);
    assertThrows(UnmanagedClassException.class, () ->
      ModInjector.getInstanceOf(MOD_ID, UnmanagedClass.class)
    );
  }

  @Test
  @DisplayName("Throws CircularDependencyException on initialization")
  void initialize_throwsOnCircularDependency() {
    assertThrows(CircularDependencyException.class, () ->
      ModInjector.initialize(MOD_ID, CircularTestModMain.class)
    );
  }

  @Test
  @DisplayName("Throws MultipleInjectableConstructorsException")
  void createInstance_throwsOnMultipleInjectConstructors() {
    assertThrows(MultipleInjectableConstructorsException.class, () ->
      ModInjector.initialize(MOD_ID, MultipleConstructorsTestModMain.class)
    );
  }

  @Test
  @DisplayName("Throws NoInjectableConstructorException")
  void createInstance_throwsOnNoInjectableConstructor() {
    assertThrows(NoInjectableConstructorException.class, () ->
      ModInjector.initialize(MOD_ID, NoConstructorTestModMain.class)
    );
  }

  @Test
  @DisplayName("Throws InstanceCreationException when constructor fails")
  void createInstance_throwsOnFailingConstructor() {
    assertThrows(InstanceCreationException.class, () ->
      ModInjector.initialize(MOD_ID, FailingConstructorTestModMain.class)
    );
  }

  @Test
  @DisplayName("Throws NoMainClassException for class without @ModMain")
  void initialize_throwsWhenMainClassHasNoAnnotation() {
    assertThrows(NoMainClassException.class, () ->
      ModInjector.initialize(MOD_ID, NoAnnotationTestModMain.class)
    );
  }

  @Test
  @DisplayName("Injects fields in superclass")
  void injectFields_injectsInSuperclass() {
    ModInjector.initialize(MOD_ID, SubServiceTestModMain.class);
    var subService = ModInjector.getInstanceOf(MOD_ID, SubService.class);

    assertDoesNotThrow(() -> {
      var field = BaseService.class.getDeclaredField("serviceB");
      field.setAccessible(true);
      var serviceB = field.get(subService);
      assertNotNull(serviceB, "Field in superclass should be injected.");
    });
  }

  @Test
  @DisplayName("Handles multiple mod containers correctly")
  void multiMod_containersAreIsolated() {
    var modId1 = "mod1";
    var modId2 = "mod2";

    ModInjector.initialize(modId1, SuccessModMain.class);
    ModInjector.initialize(modId2, SuccessModMain.class);

    var serviceA_mod1 = ModInjector.getInstanceOf(modId1, ServiceA.class);
    var serviceA_mod2 = ModInjector.getInstanceOf(modId2, ServiceA.class);

    assertNotNull(serviceA_mod1);
    assertNotNull(serviceA_mod2);
    assertNotSame(serviceA_mod1, serviceA_mod2, "Instances from different containers should be different.");
  }
}
