package org.example.minispring.injector;

import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.container.BeanFactory;
import org.example.minispring.container.SimpleBeanFactory;
import org.example.minispring.exception.CircularDependencyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyInjectorTest {

    static class NoDependencyClass {
        public NoDependencyClass() {
        }
    }

    static class DependencyClass {
        public DependencyClass() {
        }
    }

    static class DependentClass {
        private final DependencyClass dependency;

        public DependentClass(DependencyClass dependency) {
            this.dependency = dependency;
        }

        public DependencyClass getDependency() {
            return dependency;
        }
    }

    static class CircularA {
        public CircularA(CircularB b) {
        }
    }

    static class CircularB {
        public CircularB(CircularA a) {
        }
    }

    @Test
    void shouldCreateBeanWithNoDependencies() {
        // Given
        DependencyInjector injector = new DependencyInjector(null);
        BeanDefinition definition = new BeanDefinition("noDependencyClass", NoDependencyClass.class);

        // When
        Object bean = injector.createBean(definition);

        // Then
        assertNotNull(bean);
        assertInstanceOf(NoDependencyClass.class, bean);
    }

    @Test
    void shouldCreateBeanWithDependencies() {
        // Given
        BeanFactory beanFactory = new SimpleBeanFactory();
        beanFactory.registerBeanDefinition(new BeanDefinition("dependencyClass", DependencyClass.class));

        DependencyInjector injector = new DependencyInjector(beanFactory);
        BeanDefinition definition = new BeanDefinition("dependentClass", DependentClass.class);

        // When
        Object bean = injector.createBean(definition);

        // Then
        assertNotNull(bean);
        assertInstanceOf(DependentClass.class, bean);

        DependentClass dependentBean = (DependentClass) bean;
        assertNotNull(dependentBean.getDependency());
        assertInstanceOf(DependencyClass.class, dependentBean.getDependency());
    }

    @Test
    void shouldDetectCircularDependency() {
        // Given
        BeanFactory beanFactory = new SimpleBeanFactory();
        beanFactory.registerBeanDefinition(new BeanDefinition("circularA", CircularA.class));
        beanFactory.registerBeanDefinition(new BeanDefinition("circularB", CircularB.class));

        DependencyInjector injector = new DependencyInjector(beanFactory);
        BeanDefinition definition = new BeanDefinition("circularA", CircularA.class);

        // When & Then
        assertThrows(CircularDependencyException.class, () -> {
            injector.createBean(definition);
        });
    }
}
