package org.example.minispring.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AutowiredAnnotationTest {

    static class TestService {
        @Autowired
        public TestService() {
        }
    }

    @Test
    void shouldHaveAutowiredAnnotationOnConstructor() throws NoSuchMethodException {
        // Given
        Constructor<?> constructor = TestService.class.getConstructor();

        // When
        boolean hasAnnotation = constructor.isAnnotationPresent(Autowired.class);

        // Then
        assertTrue(hasAnnotation);
    }
}
