package org.example.minispring.container;

import org.example.minispring.bean.BeanDefinition;

public interface BeanFactory {
    void registerBeanDefinition(BeanDefinition definition);

    boolean containsBean(String beanName);

    Object getBean(String beanName);

    <T> T getBean(Class<T> type);
}
