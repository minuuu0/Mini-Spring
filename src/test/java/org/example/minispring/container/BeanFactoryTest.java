package org.example.minispring.container;

import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.exception.NoSuchBeanException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanFactoryTest {

    static class TestBean {
    }

    @Test
    void shouldRegisterBeanDefinition() {
        // Given
        BeanFactory beanFactory = new SimpleBeanFactory();
        BeanDefinition definition = new BeanDefinition("testBean", TestBean.class);

        // When
        beanFactory.registerBeanDefinition(definition);

        // Then
        assertTrue(beanFactory.containsBean("testBean"));
    }

    @Test
    void shouldThrowExceptionWhenBeanNotFound() {
        // Given
        BeanFactory beanFactory = new SimpleBeanFactory();

        // When & Then
        assertThrows(NoSuchBeanException.class, () -> {
            beanFactory.getBean("nonExistentBean");
        });
    }

    @Test
    void shouldCreateAndReturnBeanInstance() {
        // Given
        BeanFactory beanFactory = new SimpleBeanFactory();
        BeanDefinition definition = new BeanDefinition("testBean", TestBean.class);
        beanFactory.registerBeanDefinition(definition);

        // When
        Object bean = beanFactory.getBean("testBean");

        // Then
        assertNotNull(bean);
        assertInstanceOf(TestBean.class, bean);
    }
}
