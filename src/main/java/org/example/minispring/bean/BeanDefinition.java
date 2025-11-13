package org.example.minispring.bean;

public class BeanDefinition {
    private final String beanName;
    private final Class<?> beanClass;

    public BeanDefinition(String beanName, Class<?> beanClass) {
        this.beanName = beanName;
        this.beanClass = beanClass;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }
}
