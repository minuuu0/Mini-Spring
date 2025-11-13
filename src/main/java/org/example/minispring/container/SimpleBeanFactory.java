package org.example.minispring.container;

import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.exception.NoSuchBeanException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleBeanFactory implements BeanFactory {
    private final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();

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

        BeanDefinition definition = beanDefinitions.get(beanName);
        return createBean(definition);
    }

    private Object createBean(BeanDefinition definition) {
        try {
            return definition.getBeanClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + definition.getBeanName(), e);
        }
    }
}
