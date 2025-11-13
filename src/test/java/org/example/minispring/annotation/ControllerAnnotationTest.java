package org.example.minispring.annotation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerAnnotationTest {

    @Controller
    static class TestController {
    }

    @Test
    void shouldHaveControllerAnnotation() {
        // Given
        Class<?> clazz = TestController.class;

        // When
        boolean hasAnnotation = clazz.isAnnotationPresent(Controller.class);

        // Then
        assertTrue(hasAnnotation);
    }
}
