package org.example.minispring.annotation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryAnnotationTest {

    @Repository
    static class TestRepository {
    }

    @Test
    void shouldHaveRepositoryAnnotation() {
        // Given
        Class<?> clazz = TestRepository.class;

        // When
        boolean hasAnnotation = clazz.isAnnotationPresent(Repository.class);

        // Then
        assertTrue(hasAnnotation);
    }
}
