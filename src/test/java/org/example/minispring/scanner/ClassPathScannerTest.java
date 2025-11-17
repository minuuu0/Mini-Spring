package org.example.minispring.scanner;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathScannerTest {

    @Test
    void shouldScanClassesInPackage() {
        // Given
        ClassPathScanner scanner = new ClassPathScanner();
        String basePackage = "org.example.minispring.scanner.testdata";

        // When
        Set<Class<?>> classes = scanner.scan(basePackage);

        // Then
        assertNotNull(classes);
        assertEquals(2, classes.size());
        assertTrue(classes.stream().anyMatch(c -> c.getSimpleName().equals("SampleClass1")));
        assertTrue(classes.stream().anyMatch(c -> c.getSimpleName().equals("SampleClass2")));
    }

    @Test
    void shouldReturnEmptySetForNonExistentPackage() {
        // Given
        ClassPathScanner scanner = new ClassPathScanner();
        String basePackage = "org.example.nonexistent";

        // When
        Set<Class<?>> classes = scanner.scan(basePackage);

        // Then
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }
}
