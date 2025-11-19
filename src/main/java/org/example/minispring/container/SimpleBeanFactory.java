package org.example.minispring.container;

import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.exception.NoSuchBeanException;
import org.example.minispring.exception.NoUniqueBeanException;
import org.example.minispring.injector.DependencyInjector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleBeanFactory implements BeanFactory {
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    private final Map<String, Object> singletonCache = new ConcurrentHashMap<>();
    private final DependencyInjector dependencyInjector;

    public SimpleBeanFactory() {
        this.dependencyInjector = new DependencyInjector(this);
    }

    @Override
    public void registerBeanDefinition(BeanDefinition definition) {
        beanDefinitions.put(definition.getBeanName(), definition);
    }

    @Override
    public boolean containsBean(String beanName) {
        return beanDefinitions.containsKey(beanName);
    }

    @Override
    public Object getBean(String beanName) {
        if (!containsBean(beanName)) {
            throw new NoSuchBeanException("No bean found with name: " + beanName);
        }

        // Check singleton cache first
        Object cached = singletonCache.get(beanName);
        if (cached != null) {
            return cached;
        }

        // Create and cache the bean
        synchronized (this) {
            // Double-check locking
            cached = singletonCache.get(beanName);
            if (cached != null) {
                return cached;
            }

            BeanDefinition definition = beanDefinitions.get(beanName);
            Object bean = createBean(definition);
            singletonCache.put(beanName, bean);
            return bean;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        List<String> matchingBeanNames = new ArrayList<>();

        // Find all beans that match the type
        for (BeanDefinition definition : beanDefinitions.values()) {
            if (type.isAssignableFrom(definition.getBeanClass())) {
                matchingBeanNames.add(definition.getBeanName());
            }
        }

        if (matchingBeanNames.isEmpty()) {
            throw new NoSuchBeanException("No bean found with type: " + type.getName());
        }

        if (matchingBeanNames.size() > 1) {
            throw new NoUniqueBeanException(
                "Expected single bean but found " + matchingBeanNames.size() +
                " beans of type " + type.getName() + ": " + matchingBeanNames
            );
        }

        return (T) getBean(matchingBeanNames.get(0));
    }

    private Object createBean(BeanDefinition definition) {
        return dependencyInjector.createBean(definition);
    }
}
