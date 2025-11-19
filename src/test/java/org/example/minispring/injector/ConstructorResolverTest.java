package org.example.minispring.injector;

import org.example.minispring.annotation.Autowired;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class ConstructorResolverTest {

    static class NoConstructorClass {
    }

    static class SingleConstructorClass {
        private final String value;

        public SingleConstructorClass(String value) {
            this.value = value;
        }
    }

    static class MultipleConstructorsClass {
        private final String value;

        public MultipleConstructorsClass() {
            this.value = "default";
        }

        public MultipleConstructorsClass(String value) {
            this.value = value;
        }
    }

    static class AutowiredConstructorClass {
        private final String value;

        public AutowiredConstructorClass() {
            this.value = "default";
        }

        @Autowired
        public AutowiredConstructorClass(String value) {
            this.value = value;
        }
    }

    @Test
    void shouldResolveDefaultConstructorWhenNoConstructorDefined() {
        // Given
        ConstructorResolver resolver = new ConstructorResolver();
        Class<?> clazz = NoConstructorClass.class;

        // When
        Constructor<?> constructor = resolver.resolve(clazz);

        // Then
        assertNotNull(constructor);
        assertEquals(0, constructor.getParameterCount());
    }

    @Test
    void shouldResolveSingleConstructor() {
        // Given
        ConstructorResolver resolver = new ConstructorResolver();
        Class<?> clazz = SingleConstructorClass.class;

        // When
        Constructor<?> constructor = resolver.resolve(clazz);

        // Then
        assertNotNull(constructor);
        assertEquals(1, constructor.getParameterCount());
    }

    @Test
    void shouldResolveDefaultConstructorWhenMultipleConstructorsExist() {
        // Given
        ConstructorResolver resolver = new ConstructorResolver();
        Class<?> clazz = MultipleConstructorsClass.class;

        // When
        Constructor<?> constructor = resolver.resolve(clazz);

        // Then
        assertNotNull(constructor);
        assertEquals(0, constructor.getParameterCount());
    }

    @Test
    void shouldResolveAutowiredConstructor() {
        // Given
        ConstructorResolver resolver = new ConstructorResolver();
        Class<?> clazz = AutowiredConstructorClass.class;

        // When
        Constructor<?> constructor = resolver.resolve(clazz);

        // Then
        assertNotNull(constructor);
        assertEquals(1, constructor.getParameterCount());
        assertTrue(constructor.isAnnotationPresent(Autowired.class));
    }
}
