package com.dotnomi.fabricdependencyinjection.java;

import com.dotnomi.fabricdependencyinjection.BeanList;
import com.dotnomi.fabricdependencyinjection.ModInjector;
import com.dotnomi.fabricdependencyinjection.exception.CircularDependencyException;
import com.dotnomi.fabricdependencyinjection.exception.ContainerAlreadyInitializedException;
import com.dotnomi.fabricdependencyinjection.exception.ContainerNotInitializedException;
import com.dotnomi.fabricdependencyinjection.exception.InstanceCreationException;
import com.dotnomi.fabricdependencyinjection.exception.MultipleInjectableConstructorsException;
import com.dotnomi.fabricdependencyinjection.exception.MultiplePostConstructMethodsException;
import com.dotnomi.fabricdependencyinjection.exception.NoInjectableConstructorException;
import com.dotnomi.fabricdependencyinjection.exception.NoMainClassException;
import com.dotnomi.fabricdependencyinjection.exception.PostConstructMethodHasParametersException;
import com.dotnomi.fabricdependencyinjection.exception.TooManyInstancesFoundException;
import com.dotnomi.fabricdependencyinjection.exception.UnmanagedClassException;
import com.dotnomi.fabricdependencyinjection.java.testclasses.ambiguous.AmbiguousService;
import com.dotnomi.fabricdependencyinjection.java.testclasses.ambiguous.AmbiguousTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.beanlist.BeanListTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.beanlist.Plugin;
import com.dotnomi.fabricdependencyinjection.java.testclasses.beanlist.PluginManager;
import com.dotnomi.fabricdependencyinjection.java.testclasses.failingconstructor.FailingConstructorTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.identifier.DataProcessor;
import com.dotnomi.fabricdependencyinjection.java.testclasses.identifier.DatabaseStorage;
import com.dotnomi.fabricdependencyinjection.java.testclasses.identifier.FileStorage;
import com.dotnomi.fabricdependencyinjection.java.testclasses.identifier.IdentifierTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.identifier.StorageService;
import com.dotnomi.fabricdependencyinjection.java.testclasses.inheritance.BaseService;
import com.dotnomi.fabricdependencyinjection.java.testclasses.circular.CircularTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.inheritance.SubServiceTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.maininjection.MainDependentService;
import com.dotnomi.fabricdependencyinjection.java.testclasses.maininjection.MainInjectionTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.multipleconstructors.MultipleConstructorsTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.noannotation.NoAnnotationTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.noconstructor.NoConstructorTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.packagevariable.PackageVariableTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.packagevariable.ServiceInSamePackage;
import com.dotnomi.fabricdependencyinjection.java.testclasses.postconstruct.failing.FailingPostConstructTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.postconstruct.multiple.MultiplePostConstructTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.postconstruct.parameter.ParameterPostConstructTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.postconstruct.success.PostConstructService;
import com.dotnomi.fabricdependencyinjection.java.testclasses.postconstruct.success.PostConstructTestModMain;
import com.dotnomi.fabricdependencyinjection.java.testclasses.unmanaged.UnmanagedClass;
import com.dotnomi.fabricdependencyinjection.java.testclasses.success.ServiceA;
import com.dotnomi.fabricdependencyinjection.java.testclasses.inheritance.SubService;
import com.dotnomi.fabricdependencyinjection.java.testclasses.success.SuccessModMain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaModInjectorTest {
  private static final String MOD_ID = "test-mod";

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() throws Exception {
    var modContainersField = ModInjector.class.getDeclaredField("modContainers");
    modContainersField.setAccessible(true);
    var modContainers = (Map<String, ?>) modContainersField.get(null);
    modContainers.clear();
  }

  @Nested
  @DisplayName("Core Lifecycle & Initialization")
  class CoreLifecycleTests {
    @Test
    @DisplayName("Successful initialization and instance retrieval")
    void initialize_andGetInstance_successfully() {
      assertDoesNotThrow(() -> ModInjector.initialize(MOD_ID, new SuccessModMain()));
      var mainInstance = ModInjector.getInstanceOf(MOD_ID, ServiceA.class);
      assertNotNull(mainInstance);
      assertNotNull(mainInstance.getServiceB());
    }

    @Test
    @DisplayName("Succeeds when packageName in @ModMain is omitted")
    void initialize_succeedsWhenPackageNameIsOmitted() {
      assertDoesNotThrow(() -> ModInjector.initialize(MOD_ID, new PackageVariableTestModMain()));
      assertNotNull(ModInjector.getInstanceOf(MOD_ID, ServiceInSamePackage.class));
    }

    @Test
    @DisplayName("Returns same instance on multiple calls (Caching)")
    void getInstanceOf_returnsSameInstance() {
      ModInjector.initialize(MOD_ID, new SuccessModMain());
      var instance1 = ModInjector.getInstanceOf(MOD_ID, ServiceA.class);
      var instance2 = ModInjector.getInstanceOf(MOD_ID, ServiceA.class);
      assertSame(instance1, instance2, "Should return the same singleton instance.");
    }

    @Test
    @DisplayName("Handles multiple mod containers correctly")
    void multiMod_containersAreIsolated() {
      var modId1 = "mod1";
      var modId2 = "mod2";

      var modMainInstance = new SuccessModMain();
      ModInjector.initialize(modId1, modMainInstance);
      ModInjector.initialize(modId2, modMainInstance);

      var serviceA_mod1 = ModInjector.getInstanceOf(modId1, ServiceA.class);
      var serviceA_mod2 = ModInjector.getInstanceOf(modId2, ServiceA.class);

      assertNotNull(serviceA_mod1);
      assertNotNull(serviceA_mod2);
      assertNotSame(serviceA_mod1, serviceA_mod2, "Instances from different containers should be different.");
    }
  }

  @Nested
  @DisplayName("Exception Handling")
  class ExceptionTests {
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
      ModInjector.initialize(MOD_ID, new SuccessModMain());
      assertThrows(ContainerAlreadyInitializedException.class, () ->
        ModInjector.initialize(MOD_ID, new SuccessModMain())
      );
    }

    @Test
    @DisplayName("Throws UnmanagedClassException for class without @ModScoped")
    void getInstanceOf_throwsIfClassNotManaged() {
      ModInjector.initialize(MOD_ID, new SuccessModMain());
      assertThrows(UnmanagedClassException.class, () ->
        ModInjector.getInstanceOf(MOD_ID, UnmanagedClass.class)
      );
    }

    @Test
    @DisplayName("Throws CircularDependencyException on initialization")
    void initialize_throwsOnCircularDependency() {
      assertThrows(CircularDependencyException.class, () ->
        ModInjector.initialize(MOD_ID, new CircularTestModMain())
      );
    }

    @Test
    @DisplayName("Throws MultipleInjectableConstructorsException")
    void createInstance_throwsOnMultipleInjectConstructors() {
      assertThrows(MultipleInjectableConstructorsException.class, () ->
        ModInjector.initialize(MOD_ID, new MultipleConstructorsTestModMain())
      );
    }

    @Test
    @DisplayName("Throws NoInjectableConstructorException")
    void createInstance_throwsOnNoInjectableConstructor() {
      assertThrows(NoInjectableConstructorException.class, () ->
        ModInjector.initialize(MOD_ID, new NoConstructorTestModMain())
      );
    }

    @Test
    @DisplayName("Throws InstanceCreationException when constructor fails")
    void createInstance_throwsOnFailingConstructor() {
      assertThrows(InstanceCreationException.class, () ->
        ModInjector.initialize(MOD_ID, new FailingConstructorTestModMain())
      );
    }

    @Test
    @DisplayName("Throws NoMainClassException for class without @ModMain")
    void initialize_throwsWhenMainClassHasNoAnnotation() {
      assertThrows(NoMainClassException.class, () ->
        ModInjector.initialize(MOD_ID, new NoAnnotationTestModMain())
      );
    }

    @Test
    @DisplayName("Throws TooManyInstancesFoundException on ambiguous manual lookup")
    void getInstanceOf_throwsOnAmbiguousRequest() {
      ModInjector.initialize(MOD_ID, new AmbiguousTestModMain());
      assertThrows(TooManyInstancesFoundException.class, () ->
        ModInjector.getInstanceOf(MOD_ID, AmbiguousService.class)
      );
    }
  }

  @Nested
  @DisplayName("@PostConstruct")
  class PostConstructTests {
    @Test
    @DisplayName("Method is called successfully after instantiation")
    void postConstruct_methodIsCalledSuccessfully() {
      assertDoesNotThrow(() -> ModInjector.initialize(MOD_ID, new PostConstructTestModMain()));
      var service = ModInjector.getInstanceOf(MOD_ID, PostConstructService.class);
      assertNotNull(service);
      assertTrue(service.isInitialized(), "@PostConstruct method should have been called.");
    }

    @Test
    @DisplayName("Executes only the first found @PostConstruct method without error")
    void postConstruct_handlesMultipleAnnotationsGracefully() {
      assertThrows(MultiplePostConstructMethodsException.class, () ->
        ModInjector.initialize(MOD_ID, new MultiplePostConstructTestModMain())
      );
    }

    @Test
    @DisplayName("Throws InstanceCreationException if method has parameters")
    void postConstruct_throwsIfMethodHasParameters() {
      assertThrows(PostConstructMethodHasParametersException.class, () ->
        ModInjector.initialize(MOD_ID, new ParameterPostConstructTestModMain())
      );
    }

    @Test
    @DisplayName("Throws InstanceCreationException if method fails")
    void postConstruct_throwsIfMethodFails() {
      assertThrows(InstanceCreationException.class, () ->
        ModInjector.initialize(MOD_ID, new FailingPostConstructTestModMain())
      );
    }
  }

  @Nested
  @DisplayName("@ModIdentifier")
  class IdentifierTests {
    @Test
    @DisplayName("Injects correct bean with @ModIdentifier on constructor parameter")
    void identifier_injectsCorrectBean_onConstructor() {
      ModInjector.initialize(MOD_ID, new IdentifierTestModMain());
      var processor = ModInjector.getInstanceOf(MOD_ID, DataProcessor.class);
      assertNotNull(processor.getFileStorage());
      assertInstanceOf(FileStorage.class, processor.getFileStorage(), "Should be FileStorage instance");
    }

    @Test
    @DisplayName("Injects correct bean with @ModIdentifier on field")
    void identifier_injectsCorrectBean_onField() {
      ModInjector.initialize(MOD_ID, new IdentifierTestModMain());
      var processor = ModInjector.getInstanceOf(MOD_ID, DataProcessor.class);
      assertNotNull(processor.getDatabaseStorage());
      assertInstanceOf(DatabaseStorage.class, processor.getDatabaseStorage(), "Should be DatabaseStorage instance");
    }

    @Test
    @DisplayName("Retrieves correct bean manually with getInstanceOf and identifier")
    void identifier_retrievesCorrectBeanManually() {
      ModInjector.initialize(MOD_ID, new IdentifierTestModMain());
      var fileStorage = ModInjector.getInstanceOf(MOD_ID, StorageService.class, "file");
      var dbStorage = ModInjector.getInstanceOf(MOD_ID, StorageService.class, "database");
      assertNotNull(fileStorage);
      assertNotNull(dbStorage);
      assertInstanceOf(FileStorage.class, fileStorage);
      assertInstanceOf(DatabaseStorage.class, dbStorage);
    }

    @Test
    @DisplayName("Throws InstanceCreationException for unknown identifier")
    void identifier_throwsWhenIdentifierIsUnknown() {
      assertThrows(InstanceCreationException.class, () -> {
        ModInjector.initialize(MOD_ID, new IdentifierTestModMain());
        ModInjector.getInstanceOf(MOD_ID, StorageService.class, "unknown-id");
      });
    }
  }

  @Nested
  @DisplayName("BeanList")
  class BeanListTests {
    @Test
    @DisplayName("Injects a list of all bean implementations")
    void beanList_injectsAllImplementations() {
      ModInjector.initialize(MOD_ID, new BeanListTestModMain());
      var pluginManager = ModInjector.getInstanceOf(MOD_ID, PluginManager.class);

      assertNotNull(pluginManager.getConstructorInjectedPlugins());
      assertEquals(2, pluginManager.getConstructorInjectedPlugins().size());
      var constructorInjectedNames = pluginManager.getConstructorInjectedPlugins().asList().stream().map(Plugin::getName).toList();
      assertTrue(constructorInjectedNames.contains("Analytics"));
      assertTrue(constructorInjectedNames.contains("Chat"));

      assertNotNull(pluginManager.getFieldInjectedPlugins());
      assertEquals(2, pluginManager.getFieldInjectedPlugins().size());
      var fieldInjectedNames = pluginManager.getFieldInjectedPlugins().asList().stream().map(Plugin::getName).toList();
      assertTrue(fieldInjectedNames.contains("Analytics"));
      assertTrue(fieldInjectedNames.contains("Chat"));
    }

    @Test
    @DisplayName("Returns empty BeanList when no implementations are found")
    void beanList_returnsEmptyList_whenNoBeansFound() {
      ModInjector.initialize(MOD_ID, new SuccessModMain());
      var plugins = ModInjector.getInstancesOf(MOD_ID, Plugin.class);
      assertNotNull(plugins);
      assertTrue(plugins.isEmpty());
    }

    @Test
    @DisplayName("BeanList record methods work as expected")
    void beanList_recordMethodsWork() {
      var list1 = List.of("A", "B");
      var beanList1 = new BeanList<>(list1);
      assertEquals(list1, beanList1.asList());
      assertEquals("A", beanList1.getFirst());
      assertEquals("A", beanList1.get(0));
      assertEquals("B", beanList1.getLast());
      assertEquals("B", beanList1.get(1));
      assertEquals(2, beanList1.size());
      assertFalse(beanList1.isEmpty());
      assertNotNull(beanList1.iterator());
      assertNotNull(beanList1.spliterator());
      beanList1.forEach(Assertions::assertNotNull);
    }
  }

  @Nested
  @DisplayName("@ModMain Integration")
  class ModMainTests {
    @Test
    @DisplayName("Injects dependencies into @ModMain instance fields")
    void mainClass_injectsFieldsSuccessfully() {
      var mainInstance = new MainInjectionTestModMain();
      ModInjector.initialize(MOD_ID, mainInstance);
      assertNotNull(mainInstance.getServiceA(), "Field in @ModMain class should be injected.");
      assertNotNull(mainInstance.getServiceA().sayHello(), "Transitive dependency should be resolved.");
    }

    @Test
    @DisplayName("Injects @ModMain instance into other beans")
    void mainClass_isInjectableIntoOtherServices() {
      var mainInstance = new MainInjectionTestModMain();
      ModInjector.initialize(MOD_ID, mainInstance);
      var dependentService = ModInjector.getInstanceOf(MOD_ID, MainDependentService.class);
      assertNotNull(dependentService.getMainInstance());
      assertSame(mainInstance, dependentService.getMainInstance(), "@ModMain instance should be injectable.");
    }

    @Test
    @DisplayName("Calls @PostConstruct method on @ModMain instance")
    void mainClass_callsPostConstruct() {
      var mainInstance = new MainInjectionTestModMain();
      ModInjector.initialize(MOD_ID, mainInstance);
      assertTrue(mainInstance.isPostConstructCalled(), "@PostConstruct method on @ModMain instance should be called.");
    }
  }

  @Nested
  @DisplayName("Inheritance")
  class InheritanceTests {
    @Test
    @DisplayName("Injects fields in superclass")
    void injectFields_injectsInSuperclass() {
      ModInjector.initialize(MOD_ID, new SubServiceTestModMain());
      var subService = ModInjector.getInstanceOf(MOD_ID, SubService.class);

      assertDoesNotThrow(() -> {
        var field = BaseService.class.getDeclaredField("serviceB");
        field.setAccessible(true);
        var serviceB = field.get(subService);
        assertNotNull(serviceB, "Field in superclass should be injected.");
      });
    }
  }
}
