package org.example.minispring.container;

import org.example.minispring.container.testdata.TestConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationDebugTest {

    @Test
    void shouldFindConfigurationClass() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When
        boolean hasTestConfig = context.containsBean("testConfig");

        // Then
        System.out.println("Has testConfig bean: " + hasTestConfig);
        assertTrue(hasTestConfig, "TestConfig should be registered as a bean");
    }

    @Test
    void shouldGetConfigurationClassInstance() {
        // Given
        ApplicationContext context = new AnnotationConfigApplicationContext(
            "org.example.minispring.container.testdata"
        );

        // When
        Object config = context.getBean("testConfig");

        // Then
        assertNotNull(config);
        assertInstanceOf(TestConfig.class, config);
    }
}
