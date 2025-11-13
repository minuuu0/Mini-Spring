package org.example.minispring.annotation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentAnnotationTest {

    @Component
    static class TestComponent {
    }

    @Test
    void shouldHaveComponentAnnotation() {
        // Given
        Class<?> clazz = TestComponent.class;

        // When
        boolean hasAnnotation = clazz.isAnnotationPresent(Component.class);

        // Then
        assertTrue(hasAnnotation);
    }
}
