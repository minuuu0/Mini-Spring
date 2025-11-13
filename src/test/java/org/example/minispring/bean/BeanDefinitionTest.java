package org.example.minispring.bean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeanDefinitionTest {

    static class TestBean {
    }

    @Test
    void shouldCreateBeanDefinitionWithName() {
        // Given
        String beanName = "testBean";
        Class<?> beanClass = TestBean.class;

        // When
        BeanDefinition definition = new BeanDefinition(beanName, beanClass);

        // Then
        assertEquals(beanName, definition.getBeanName());
    }

    @Test
    void shouldReturnBeanClass() {
        // Given
        String beanName = "testBean";
        Class<?> beanClass = TestBean.class;

        // When
        BeanDefinition definition = new BeanDefinition(beanName, beanClass);

        // Then
        assertEquals(beanClass, definition.getBeanClass());
    }
}
