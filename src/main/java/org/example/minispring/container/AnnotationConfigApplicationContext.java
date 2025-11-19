package org.example.minispring.container;

import org.example.minispring.bean.BeanDefinition;
import org.example.minispring.scanner.ComponentScanner;

import java.util.Set;

public class AnnotationConfigApplicationContext implements ApplicationContext {
    private final BeanFactory beanFactory;
    private final ComponentScanner componentScanner;

    public AnnotationConfigApplicationContext(String basePackage) {
        this.beanFactory = new SimpleBeanFactory();
        this.componentScanner = new ComponentScanner();

        refresh(basePackage);
    }

    private void refresh(String basePackage) {
        // Scan and register all components
        Set<BeanDefinition> beanDefinitions = componentScanner.scan(basePackage);
        for (BeanDefinition definition : beanDefinitions) {
            beanFactory.registerBeanDefinition(definition);
        }
    }

    @Override
    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    @Override
    public <T> T getBean(Class<T> type) {
        return beanFactory.getBean(type);
    }

    @Override
    public boolean containsBean(String beanName) {
        return beanFactory.containsBean(beanName);
    }
}
