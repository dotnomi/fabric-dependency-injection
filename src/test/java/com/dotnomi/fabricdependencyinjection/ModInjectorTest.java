package com.dotnomi.fabricdependencyinjection;

import com.dotnomi.fabricdependencyinjection.testclasses.ServiceA;
import com.dotnomi.fabricdependencyinjection.testclasses.TestModMain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
  @DisplayName("Successful initialization and instance retrieval")
  void initialize_andGetInstance_successfully() {
    // When
    assertDoesNotThrow(() -> ModInjector.initialize(MOD_ID, TestModMain.class));
    TestModMain mainInstance = ModInjector.getInstanceOf(MOD_ID, TestModMain.class);

    // Then
    assertNotNull(mainInstance);
    assertNotNull(mainInstance.serviceA);
    assertNotNull(mainInstance.serviceA.getServiceB());
  }

  @Test
  @DisplayName("Returns same instance on multiple calls (Caching)")
  void getInstanceOf_returnsSameInstance() {
    // Given
    ModInjector.initialize(MOD_ID, TestModMain.class);

    // When
    ServiceA instance1 = ModInjector.getInstanceOf(MOD_ID, ServiceA.class);
    ServiceA instance2 = ModInjector.getInstanceOf(MOD_ID, ServiceA.class);

    // Then
    assertSame(instance1, instance2, "Should return the same singleton instance.");
  }
}
