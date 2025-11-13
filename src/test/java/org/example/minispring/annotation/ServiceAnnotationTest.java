package org.example.minispring.annotation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceAnnotationTest {

    @Service
    static class TestService {
    }

    @Test
    void shouldHaveServiceAnnotation() {
        // Given
        Class<?> clazz = TestService.class;

        // When
        boolean hasAnnotation = clazz.isAnnotationPresent(Service.class);

        // Then
        assertTrue(hasAnnotation);
    }
}
