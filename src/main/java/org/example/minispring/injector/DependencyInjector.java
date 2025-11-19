package org.example.minispring.injector;

import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.container.BeanFactory;
import org.example.minispring.exception.CircularDependencyException;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

public class DependencyInjector {

    private final BeanFactory beanFactory;
    private final ConstructorResolver constructorResolver;
    private final ThreadLocal<Set<Class<?>>> beingCreated = ThreadLocal.withInitial(HashSet::new);

    public DependencyInjector(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.constructorResolver = new ConstructorResolver();
    }

    public Object createBean(BeanDefinition definition) {
        Class<?> beanClass = definition.getBeanClass();

        // Check for circular dependency
        if (beingCreated.get().contains(beanClass)) {
            throw new CircularDependencyException("Circular dependency detected for bean: " + definition.getBeanName());
        }

        beingCreated.get().add(beanClass);

        try {
            Constructor<?> constructor = constructorResolver.resolve(beanClass);
            constructor.setAccessible(true);

            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }

            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] dependencies = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                dependencies[i] = resolveDependency(parameterTypes[i]);
            }

            return constructor.newInstance(dependencies);
        } catch (CircularDependencyException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + definition.getBeanName(), e);
        } finally {
            beingCreated.get().remove(beanClass);
            if (beingCreated.get().isEmpty()) {
                beingCreated.remove();
            }
        }
    }

    private Object resolveDependency(Class<?> type) {
        String beanName = generateBeanName(type);
        return beanFactory.getBean(beanName);
    }

    private String generateBeanName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
