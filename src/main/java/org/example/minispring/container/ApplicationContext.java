package org.example.minispring.container;

public interface ApplicationContext {
    Object getBean(String beanName);
    <T> T getBean(Class<T> type);
    boolean containsBean(String beanName);
}
