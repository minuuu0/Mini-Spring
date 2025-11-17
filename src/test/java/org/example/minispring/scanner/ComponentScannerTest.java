package org.example.minispring.scanner;

import org.example.minispring.bean.BeanDefinition;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComponentScannerTest {

    @Test
    void shouldScanComponentAnnotatedClasses() {
        // Given
        ComponentScanner scanner = new ComponentScanner();
        String basePackage = "org.example.minispring.scanner.testdata";

        // When
        Set<BeanDefinition> beanDefinitions = scanner.scan(basePackage);

        // Then
        assertNotNull(beanDefinitions);
        assertEquals(2, beanDefinitions.size());

        assertTrue(beanDefinitions.stream()
                .anyMatch(bd -> bd.getBeanClass().getSimpleName().equals("ComponentClass")));
        assertTrue(beanDefinitions.stream()
                .anyMatch(bd -> bd.getBeanClass().getSimpleName().equals("ServiceClass")));
    }

    @Test
    void shouldGenerateBeanNameFromClassName() {
        // Given
        ComponentScanner scanner = new ComponentScanner();
        String basePackage = "org.example.minispring.scanner.testdata";

        // When
        Set<BeanDefinition> beanDefinitions = scanner.scan(basePackage);

        // Then
        assertTrue(beanDefinitions.stream()
                .anyMatch(bd -> bd.getBeanName().equals("componentClass")));
        assertTrue(beanDefinitions.stream()
                .anyMatch(bd -> bd.getBeanName().equals("serviceClass")));
    }

    @Test
    void shouldReturnEmptySetForNonComponentPackage() {
        // Given
        ComponentScanner scanner = new ComponentScanner();
        String basePackage = "org.example.nonexistent";

        // When
        Set<BeanDefinition> beanDefinitions = scanner.scan(basePackage);

        // Then
        assertNotNull(beanDefinitions);
        assertTrue(beanDefinitions.isEmpty());
    }
}
