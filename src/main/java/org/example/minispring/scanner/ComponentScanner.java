package org.example.minispring.scanner;

import org.example.minispring.annotation.Component;
import org.example.minispring.annotation.Controller;
import org.example.minispring.annotation.Repository;
import org.example.minispring.annotation.Service;
import org.example.minispring.bean.BeanDefinition;

import java.util.HashSet;
import java.util.Set;

public class ComponentScanner {

    private final ClassPathScanner classPathScanner;

    public ComponentScanner() {
        this.classPathScanner = new ClassPathScanner();
    }

    public Set<BeanDefinition> scan(String basePackage) {
        Set<BeanDefinition> beanDefinitions = new HashSet<>();
        Set<Class<?>> classes = classPathScanner.scan(basePackage);

        for (Class<?> clazz : classes) {
            if (isComponent(clazz)) {
                String beanName = generateBeanName(clazz);
                beanDefinitions.add(new BeanDefinition(beanName, clazz));
            }
        }

        return beanDefinitions;
    }

    private boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class) ||
                clazz.isAnnotationPresent(Service.class) ||
                clazz.isAnnotationPresent(Repository.class) ||
                clazz.isAnnotationPresent(Controller.class);
    }

    private String generateBeanName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
